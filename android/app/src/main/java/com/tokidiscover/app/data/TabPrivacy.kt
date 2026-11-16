package com.tokidiscover.app.data

/**
 * Private tabs skip history and use a no-cache WebView.
 * They must not call CookieManager.setAcceptCookie(false): that switch is process-wide
 * and would sign normal tabs out of every site.
 */
object TabPrivacy {
    const val touchesProcessCookieJar: Boolean = false

    fun recordHistory(isPrivate: Boolean, url: String): Boolean {
        if (isPrivate) return false
        if (url.equals(UrlNormalizer.NEW_TAB_URL, ignoreCase = true)) return false
        return UrlNormalizer.isSafeScheme(url)
    }
}
