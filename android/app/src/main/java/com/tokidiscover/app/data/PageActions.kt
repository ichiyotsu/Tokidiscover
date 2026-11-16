package com.tokidiscover.app.data

import java.net.URLEncoder

/** In-page translation goes through the public Google Translate URL. WebView has no translate API. */
object PageActions {
    fun translateUrl(pageUrl: String, targetLanguage: String = "zh-CN"): String {
        val encoded = URLEncoder.encode(pageUrl, "UTF-8")
        return "https://translate.google.com/translate?sl=auto&tl=$targetLanguage&u=$encoded"
    }
}
