package com.tokidiscover.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class BottomBarStateTest {
    @Test
    fun websiteAlwaysShowsTheFunctionBar() {
        assertEquals(BottomBarState.FunctionBar, bottomBarState(showingPage = true))
    }

    @Test
    fun appPagesShowTheRegularNavBar() {
        assertEquals(BottomBarState.Nav, bottomBarState(showingPage = false))
    }
}
