package com.tokidiscover.app.data

/**
 * What the WebView should do with a navigation URL.
 *
 * Pure decision logic, kept out of [android.webkit.WebViewClient] so it can
 * be unit tested without Android. The actual external launch lives in the
 * browser host, which owns the [android.content.Context].
 */
sealed interface SchemeAction {
    /** http(s) and about: URLs load inside the WebView. */
    data object LoadInWebView : SchemeAction

    /** App schemes (tel:, mailto:, intent:, weixin://, …) go to an external app. */
    data object OpenExternally : SchemeAction

    /** Script/data/file URLs never load and never leave the app. */
    data object Blocked : SchemeAction
}

object SchemePolicy {
    private val blockedSchemes = listOf("javascript:", "data:", "vbscript:", "file:", "blob:")

    fun classify(url: String): SchemeAction {
        val lower = url.trim().lowercase()
        return when {
            lower.startsWith("http://") || lower.startsWith("https://") -> SchemeAction.LoadInWebView
            lower.startsWith("about:") -> SchemeAction.LoadInWebView
            blockedSchemes.any { lower.startsWith(it) } -> SchemeAction.Blocked
            // Any other scheme (tel:, mailto:, sms:, intent:, weixin://, alipays://, …)
            // belongs to another app. Scheme-less input is a relative URL and stays.
            lower.contains(':') -> SchemeAction.OpenExternally
            else -> SchemeAction.LoadInWebView
        }
    }
}
