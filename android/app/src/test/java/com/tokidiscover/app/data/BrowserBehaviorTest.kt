package com.tokidiscover.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserBehaviorTest {
    @Test
    fun privateTabsDoNotTouchTheProcessCookieJarOrHistory() {
        assertFalse(TabPrivacy.touchesProcessCookieJar)
        assertFalse(TabPrivacy.recordHistory(isPrivate = true, url = "https://example.com"))
        assertFalse(TabPrivacy.recordHistory(isPrivate = false, url = UrlNormalizer.NEW_TAB_URL))
        assertTrue(TabPrivacy.recordHistory(isPrivate = false, url = "https://example.com"))
    }

    @Test
    fun translateUrlKeepsThePageAndTargetsChinese() {
        val url = PageActions.translateUrl("https://example.com/a b")
        assertTrue(url.startsWith("https://translate.google.com/translate?"))
        assertTrue(url.contains("tl=zh-CN"))
        assertTrue(url.contains("u=https%3A%2F%2Fexample.com%2Fa+b"))
    }

    @Test
    fun updateComparisonUsesNumericVersions() {
        assertTrue(UpdateChecker.isNewer("0.1.1", "0.1.0"))
        assertFalse(UpdateChecker.isNewer("0.1.0", "0.1.0"))
        assertFalse(UpdateChecker.isNewer("0.1.0", "0.1.1"))
        assertTrue(UpdateChecker.isNewer("0.2.0", "0.1.9"))
        assertFalse(UpdateChecker.isNewer("0.1.0-beta1", "0.1.0"))
    }
}
