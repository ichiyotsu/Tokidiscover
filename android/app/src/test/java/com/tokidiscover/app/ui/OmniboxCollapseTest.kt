package com.tokidiscover.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OmniboxCollapseTest {
    @Test
    fun scrollingDownCollapsesAndScrollingUpExpands() {
        assertTrue(nextOmniboxCollapsed(collapsed = false, scrollDeltaY = 20, editing = false))
        assertFalse(nextOmniboxCollapsed(collapsed = true, scrollDeltaY = -20, editing = false))
    }

    @Test
    fun smallMovementKeepsTheCurrentState() {
        assertFalse(nextOmniboxCollapsed(collapsed = false, scrollDeltaY = 4, editing = false))
        assertTrue(nextOmniboxCollapsed(collapsed = true, scrollDeltaY = -4, editing = false))
    }

    @Test
    fun editingAlwaysExpands() {
        assertFalse(nextOmniboxCollapsed(collapsed = true, scrollDeltaY = 40, editing = true))
    }
}
