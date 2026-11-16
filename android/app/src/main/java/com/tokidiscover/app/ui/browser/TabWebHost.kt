package com.tokidiscover.app.ui.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.tokidiscover.app.data.BrowserTab
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.ui.BrowserViewModel

private const val DESKTOP_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36"

/** Keeps one WebView per tab so switching tabs does not throw the page away. */
@SuppressLint("SetJavaScriptEnabled")
class TabWebHost(context: Context) : FrameLayout(context) {
    var controller = WebController()
    private val views = linkedMapOf<String, WebView>()
    private val requested = mutableMapOf<String, String>()

    fun navigate(tab: BrowserTab, url: String) {
        val web = views.getOrPut(tab.id) {
            createWebView(tab).also { created ->
                addView(created, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            }
        }
        requested[tab.id] = url
        web.loadUrl(url)
    }

    fun sync(vm: BrowserViewModel, showPage: Boolean, backgroundColor: Int) {
        val alive = vm.tabs.map { it.id }.toSet()
        views.keys.filter { it !in alive }.toList().forEach { destroyTab(it) }
        for (tab in vm.tabs) {
            val web = views.getOrPut(tab.id) { createWebView(tab) }
            if (web.parent == null) {
                addView(web, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            }
            applySettings(web, tab, vm)
            web.setBackgroundColor(backgroundColor)
            val active = tab.id == vm.activeTabId && showPage
            web.visibility = if (active) VISIBLE else GONE
            if (active) web.onResume() else web.onPause()
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

    private fun createWebView(tab: BrowserTab): WebView {
        return WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.safeBrowsingEnabled = true
            settings.cacheMode = if (tab.isPrivate) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, !tab.isPrivate)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url?.toString().orEmpty()
                    if (!UrlNormalizer.isSafeScheme(url)) {
                        controller.onBlocked("已拦截危险协议")
                        return true
                    }
                    return false
                }

                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                    if (url != null) controller.onPageStarted(tab, url)
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    controller.onPageFinished(
                        tab,
                        view.title.orEmpty(),
                        url ?: tab.url,
                        view.canGoBack(),
                        view.canGoForward(),
                    )
                }

                override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                    controller.onHistoryChanged(tab, url ?: tab.url, view.canGoBack(), view.canGoForward())
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

    fun back() {
        webView?.takeIf { it.canGoBack() }?.goBack()
    }

    fun forward() {
        webView?.takeIf { it.canGoForward() }?.goForward()
    }

    fun reloadOrStop(loading: Boolean) {
        val web = webView ?: return
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
