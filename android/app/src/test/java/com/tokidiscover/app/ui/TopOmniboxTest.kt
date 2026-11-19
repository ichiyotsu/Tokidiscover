package com.tokidiscover.app.ui

import com.tokidiscover.app.data.BrowserTab
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.ui.components.topOmniboxText
import org.junit.Assert.assertEquals
import org.junit.Test

class TopOmniboxTest {
    @Test
    fun websiteShowsTheFullUrl() {
        val tab = BrowserTab(url = "https://www.bilibili.com/video/BV1xx")
        assertEquals("https://www.bilibili.com/video/BV1xx", topOmniboxText(tab))
    }

    @Test
    fun privateTabPrefixesIncognito() {
        val tab = BrowserTab(url = "https://example.com/", isPrivate = true)
        assertEquals("无痕 · https://example.com/", topOmniboxText(tab))
    }

    @Test
    fun newTabShowsThePlaceholder() {
        val tab = BrowserTab(url = UrlNormalizer.NEW_TAB_URL)
        assertEquals("搜索或输入网址", topOmniboxText(tab))
    }
}
