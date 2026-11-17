package com.tokidiscover.app.ui.browser

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.text.Html
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.tokidiscover.app.data.BrowserTab
import com.tokidiscover.app.data.SchemeAction
import com.tokidiscover.app.data.SchemePolicy
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.data.WebPermissionPolicy
import com.tokidiscover.app.ui.BrowserViewModel

private const val DESKTOP_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/157.0.0.0 Safari/537.36"

/** Settings + visibility last applied to a WebView. sync() skips work when nothing changed. */
private data class AppliedState(
    val javaScriptEnabled: Boolean,
    val desktopUa: Boolean,
    val cacheMode: Int,
    val backgroundColor: Int,
    val active: Boolean,
)

/** Friendly page shown for main-frame load failures instead of a blank screen. */
private const val ERROR_PAGE_HTML = """
<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1">
<title>页面打不开</title>
<style>
@media (prefers-color-scheme: dark) { body { background: #141218; color: #e6e0e9; } .url { color: #cac4d0; } }
body { font-family: sans-serif; padding: 32px 24px; }
h1 { font-size: 20px; }
.url { word-break: break-all; font-size: 14px; }
.reason { font-size: 14px; opacity: .8; }
</style></head><body>
<h1>页面打不开</h1>
<p class="url">{URL}</p>
<p class="reason">{REASON}</p>
<p class="reason">检查网络后，点按底栏的刷新按钮重试。</p>
</body></html>
"""

/** Keeps one WebView per tab so switching tabs does not throw the page away. */
@SuppressLint("SetJavaScriptEnabled")
class TabWebHost(context: Context) : FrameLayout(context) {
    var controller = WebController()
    private val views = linkedMapOf<String, WebView>()
    private val requested = mutableMapOf<String, String>()
    private val applied = mutableMapOf<WebView, AppliedState>()

    /** Last surface color sync() saw. New WebViews start with it so the first paint is not white. */
    private var backgroundColor: Int = Color.TRANSPARENT

    fun navigate(tab: BrowserTab, url: String) {
        val web = views.getOrPut(tab.id) {
            createWebView(tab).also { created ->
                addView(created, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            }
        }
        controller.errorUrl = null
        requested[tab.id] = url
        web.loadUrl(url)
    }

    fun sync(vm: BrowserViewModel, showPage: Boolean, backgroundColor: Int) {
        this.backgroundColor = backgroundColor
        val alive = vm.tabs.map { it.id }.toSet()
        views.keys.filter { it !in alive }.toList().forEach { destroyTab(it) }
        for (tab in vm.tabs) {
            val web = views.getOrPut(tab.id) { createWebView(tab) }
            if (web.parent == null) {
                addView(web, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            }
            val desired = AppliedState(
                javaScriptEnabled = vm.settings.javaScriptEnabled,
                desktopUa = vm.settings.desktopUa,
                cacheMode = if (tab.isPrivate) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT,
                backgroundColor = backgroundColor,
                active = tab.id == vm.activeTabId && showPage,
            )
            if (applied[web] != desired) {
                applySettings(web, tab, vm)
                web.setBackgroundColor(backgroundColor)
                web.visibility = if (desired.active) VISIBLE else GONE
                if (desired.active) web.onResume() else web.onPause()
                applied[web] = desired
            }
        }
        val activeTab = vm.activeTab
        val activeWeb = views[activeTab.id]
        controller.webView = if (showPage) activeWeb else null
        if (activeWeb != null && vm.pendingNavigation == null) loadRestored(activeWeb, activeTab)
    }

    fun clearCaches() {
        views.values.forEach { it.clearCache(true) }
    }

    private fun loadRestored(web: WebView, tab: BrowserTab) {
        if (web.url.isNullOrBlank() && requested[tab.id] == null && tab.url != UrlNormalizer.NEW_TAB_URL) {
            requested[tab.id] = tab.url
            web.loadUrl(tab.url)
        }
    }

    private fun applySettings(web: WebView, tab: BrowserTab, vm: BrowserViewModel) {
        web.settings.javaScriptEnabled = vm.settings.javaScriptEnabled
        web.settings.userAgentString = if (vm.settings.desktopUa) {
            DESKTOP_UA
        } else {
            WebSettings.getDefaultUserAgent(context)
        }
        web.settings.cacheMode = if (tab.isPrivate) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /**
     * Tries to hand [url] to another app. Returns true when an app took it, or
     * when an intent:// fallback URL was loaded back into [view].
     */
    private fun openExternal(view: WebView, url: String): Boolean {
        return try {
            val intent = if (url.startsWith("intent://", ignoreCase = true)) {
                Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                // Launch directly: on Android 11+ resolveActivity() needs
                // <queries> entries and would wrongly report installed apps
                // as missing. A failed launch throws instead.
                context.startActivity(intent)
                true
            } catch (_: RuntimeException) {
                val fallback = intent.getStringExtra("browser_fallback_url")
                if (!fallback.isNullOrBlank() && UrlNormalizer.isSafeScheme(fallback)) {
                    view.loadUrl(fallback)
                    true
                } else {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun showErrorPage(view: WebView, failingUrl: String, description: String) {
        controller.errorUrl = failingUrl
        val html = ERROR_PAGE_HTML
            .replace("{URL}", Html.escapeHtml(failingUrl))
            .replace("{REASON}", Html.escapeHtml(description))
        view.loadDataWithBaseURL(failingUrl, html, "text/html", "UTF-8", null)
    }

    private fun createWebView(tab: BrowserTab): WebView {
        return WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            // Paint the surface color before the first loadUrl so the page never flashes white.
            setBackgroundColor(backgroundColor)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.safeBrowsingEnabled = true
            settings.cacheMode = if (tab.isPrivate) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            settings.setSupportMultipleWindows(true)
            settings.javaScriptCanOpenWindowsAutomatically = true
            // Setter-only API: no Kotlin property access.
            settings.setGeolocationEnabled(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, !tab.isPrivate)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    if (!request.isForMainFrame) return false
                    val url = request.url?.toString().orEmpty()
                    return when (SchemePolicy.classify(url)) {
                        SchemeAction.LoadInWebView -> false
                        SchemeAction.Blocked -> {
                            controller.onBlocked("已拦截危险协议")
                            true
                        }
                        SchemeAction.OpenExternally -> {
                            if (!openExternal(view, url)) controller.onBlocked("没有应用可以打开这个链接")
                            true
                        }
                    }
                }

                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                    if (url != null) controller.onPageStarted(tab, url)
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    val finished = url ?: tab.url
                    if (finished != controller.errorUrl) controller.errorUrl = null
                    controller.onPageFinished(
                        tab,
                        view.title.orEmpty(),
                        finished,
                        view.canGoBack(),
                        view.canGoForward(),
                    )
                }

                override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                    controller.onHistoryChanged(tab, url ?: tab.url, view.canGoBack(), view.canGoForward())
                }

                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    if (!request.isForMainFrame) return
                    showErrorPage(view, request.url.toString(), error.description.toString())
                }

                @Deprecated("Deprecated in Java")
                override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                    showErrorPage(view, failingUrl, description)
                }

                override fun onReceivedHttpError(
                    view: WebView,
                    request: WebResourceRequest,
                    errorResponse: WebResourceResponse,
                ) {
                    // Only server errors get the friendly page; 4xx responses usually carry
                    // the site's own error page, which must stay visible.
                    if (!request.isForMainFrame || errorResponse.statusCode < 500) return
                    showErrorPage(view, request.url.toString(), "服务器返回了 ${errorResponse.statusCode}")
                }

                override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                    // Never proceed past a certificate error.
                    handler.cancel()
                    controller.onBlocked("证书有问题，已停止加载此页面")
                }

                override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                    post {
                        destroyTab(tab.id)
                        controller.onRendererGone()
                    }
                    return true
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    tab.progress = newProgress
                }

                override fun onCreateWindow(
                    view: WebView,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message,
                ): Boolean {
                    val transport = resultMsg.obj as? WebView.WebViewTransport ?: return false
                    var handled = false
                    val popup = WebView(view.context)
                    fun open(url: String) {
                        if (handled) return
                        handled = true
                        controller.onOpenInNewTab(url, tab.isPrivate)
                        popup.postDelayed({ runCatching { popup.destroy() } }, 1000L)
                    }
                    popup.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(v: WebView, request: WebResourceRequest): Boolean {
                            open(request.url.toString())
                            return true
                        }

                        override fun onPageStarted(v: WebView, url: String?, favicon: Bitmap?) {
                            if (url != null && url != UrlNormalizer.NEW_TAB_URL) open(url)
                        }
                    }
                    transport.webView = popup
                    resultMsg.sendToTarget()
                    return true
                }

                override fun onShowFileChooser(
                    webView: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams,
                ): Boolean {
                    controller.fileChooserCallback?.onReceiveValue(null)
                    controller.fileChooserCallback = filePathCallback
                    controller.onFileChooserRequested(fileChooserParams.mode, fileChooserParams.acceptTypes)
                    return true
                }

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String,
                    callback: GeolocationPermissions.Callback,
                ) {
                    val allowed = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (allowed) {
                        callback.invoke(origin, true, false)
                    } else {
                        controller.onGeolocationRequest(origin, callback)
                    }
                }

                override fun onPermissionRequest(request: PermissionRequest) {
                    val needed = WebPermissionPolicy.systemPermissionsFor(request.resources)
                    if (needed.isEmpty()) {
                        // Nothing grantable (e.g. protected media only): deny as before.
                        request.deny()
                        return
                    }
                    val granted = needed.filter { hasPermission(it) }.toSet()
                    if (granted.containsAll(needed)) {
                        request.grant(WebPermissionPolicy.grantableResources(request.resources, granted))
                    } else {
                        controller.onWebPermissionRequest(request)
                    }
                }
            }
            setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                controller.onDownload(url, userAgent, contentDisposition, mimeType, contentLength)
            }
            setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (visibility == VISIBLE) controller.onScroll(scrollY - oldScrollY)
            }
        }
    }

    private fun destroyTab(id: String) {
        val web = views.remove(id) ?: return
        requested.remove(id)
        applied.remove(web)
        removeView(web)
        web.destroy()
        if (controller.webView === web) controller.webView = null
    }
}

class WebController {
    var webView: WebView? = null
    var onPageStarted: (BrowserTab, String) -> Unit = { _, _ -> }
    var onPageFinished: (BrowserTab, String, String, Boolean, Boolean) -> Unit = { _, _, _, _, _ -> }
    var onHistoryChanged: (BrowserTab, String, Boolean, Boolean) -> Unit = { _, _, _, _ -> }
    var onBlocked: (String) -> Unit = {}
    var onDownload: (String, String, String, String, Long) -> Unit = { _, _, _, _, _ -> }
    var onScroll: (Int) -> Unit = {}
    var onRendererGone: () -> Unit = {}
    var onOpenInNewTab: (String, Boolean) -> Unit = { _, _ -> }
    var onFileChooserRequested: (Int, Array<String>) -> Unit = { _, _ -> }
    /** A page wants camera/microphone and the app lacks the system permission: the shell prompts. */
    var onWebPermissionRequest: (PermissionRequest) -> Unit = { it.deny() }
    /** A page wants the location and the app lacks the system permission: the shell prompts. */
    var onGeolocationRequest: (String, GeolocationPermissions.Callback) -> Unit =
        { origin, callback -> callback.invoke(origin, false, false) }
    var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    /** URL whose friendly error page is currently shown, so refresh reloads the real page. */
    var errorUrl: String? = null

    fun deliverFileChooserResult(uris: Array<Uri>?) {
        fileChooserCallback?.onReceiveValue(uris)
        fileChooserCallback = null
    }

    fun back() {
        webView?.takeIf { it.canGoBack() }?.goBack()
    }

    fun forward() {
        webView?.takeIf { it.canGoForward() }?.goForward()
    }

    fun reloadOrStop(loading: Boolean) {
        val web = webView ?: return
        val failed = errorUrl
        if (failed != null) {
            errorUrl = null
            web.loadUrl(failed)
            return
        }
        if (loading) web.stopLoading() else web.reload()
    }

    fun find(query: String) {
        webView?.findAllAsync(query)
    }

    fun findNext() {
        webView?.findNext(true)
    }

    fun clearMatches() {
        webView?.clearMatches()
    }
}
