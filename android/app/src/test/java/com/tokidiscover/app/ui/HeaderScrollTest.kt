package com.tokidiscover.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeaderScrollTest {
    @Test
    fun downwardScrollAccumulatesNegativeOffset() {
        assertEquals(-20f, headerScrollOffset(0f, -20f, 0f))
        assertEquals(-32f, headerScrollOffset(-12f, -20f, 0f))
    }

    @Test
    fun pullingPastTheTopClearsTheOffset() {
        assertEquals(0f, headerScrollOffset(-10f, 0f, 4f))
    }

    @Test
    fun offsetDoesNotBecomePositive() {
        assertEquals(0f, headerScrollOffset(0f, 8f, 0f))
    }

    @Test
    fun overlapStartsAfterTheThreshold() {
        assertFalse(headerOverlapsContent(-4f, 8f))
        assertTrue(headerOverlapsContent(-9f, 8f))
    }
}
