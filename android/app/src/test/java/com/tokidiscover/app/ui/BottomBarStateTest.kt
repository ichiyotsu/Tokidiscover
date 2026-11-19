package com.tokidiscover.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class BottomBarStateTest {
    @Test
    fun websiteShowsOnlyTheDomainCapsule() {
        assertEquals(BottomBarState.Capsule, bottomBarState(showingPage = true))
    }

    @Test
    fun appPagesShowTheRegularNavBar() {
        assertEquals(BottomBarState.Nav, bottomBarState(showingPage = false))
    }

    @Test
    fun mainDomainStripsSchemeWwwAndPath() {
        assertEquals("bilibili.com", mainDomain("https://www.bilibili.com/video/BV1xx"))
        assertEquals("example.com:8080", mainDomain("http://example.com:8080/a?b=1"))
        assertEquals("192.168.1.1", mainDomain("http://192.168.1.1/"))
        assertEquals("", mainDomain(""))
    }
}
