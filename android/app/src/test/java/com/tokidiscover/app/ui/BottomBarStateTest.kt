package com.tokidiscover.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class BottomBarStateTest {
    @Test
    fun homeAndAppPagesShowTheRegularNavBar() {
        assertEquals(BottomBarState.Nav, bottomBarState(showingPage = false, collapsed = false, editing = false))
        assertEquals(BottomBarState.Nav, bottomBarState(showingPage = false, collapsed = true, editing = false))
    }

    @Test
    fun websiteShowsTheFunctionBarUntilScrolled() {
        assertEquals(BottomBarState.FunctionBar, bottomBarState(showingPage = true, collapsed = false, editing = false))
        assertEquals(BottomBarState.Capsule, bottomBarState(showingPage = true, collapsed = true, editing = false))
    }

    @Test
    fun editingAlwaysOpensTheFunctionBarOnAWebsite() {
        assertEquals(BottomBarState.FunctionBar, bottomBarState(showingPage = true, collapsed = true, editing = true))
    }

    @Test
    fun mainDomainStripsSchemeWwwAndPath() {
        assertEquals("bilibili.com", mainDomain("https://www.bilibili.com/video/BV1xx"))
        assertEquals("example.com:8080", mainDomain("http://example.com:8080/a?b=1"))
        assertEquals("192.168.1.1", mainDomain("http://192.168.1.1/"))
        assertEquals("", mainDomain(""))
    }
}
