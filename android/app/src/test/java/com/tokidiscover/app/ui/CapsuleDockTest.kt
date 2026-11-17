package com.tokidiscover.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class CapsuleDockTest {
    @Test
    fun dockOffsetsSpanTheFreeTravel() {
        assertEquals(0f, capsuleOffsetFor(CapsuleDock.Left, 336f, 200f), 0.001f)
        assertEquals(68f, capsuleOffsetFor(CapsuleDock.Center, 336f, 200f), 0.001f)
        assertEquals(136f, capsuleOffsetFor(CapsuleDock.Right, 336f, 200f), 0.001f)
    }

    @Test
    fun capsuleWiderThanContainerStaysPut() {
        assertEquals(0f, capsuleOffsetFor(CapsuleDock.Right, 100f, 200f), 0.001f)
        assertEquals(CapsuleDock.Center, nearestCapsuleDock(0f, 100f, 200f, 0f))
    }

    @Test
    fun slowReleaseSnapsToTheNearestDock() {
        assertEquals(CapsuleDock.Left, nearestCapsuleDock(10f, 336f, 200f, 0f))
        assertEquals(CapsuleDock.Center, nearestCapsuleDock(68f, 336f, 200f, 0f))
        assertEquals(CapsuleDock.Right, nearestCapsuleDock(120f, 336f, 200f, 0f))
    }

    @Test
    fun fastFlingMovesOneDockInTheFlingDirection() {
        assertEquals(CapsuleDock.Center, nearestCapsuleDock(10f, 336f, 200f, 900f))
        assertEquals(CapsuleDock.Right, nearestCapsuleDock(68f, 336f, 200f, 900f))
        assertEquals(CapsuleDock.Right, nearestCapsuleDock(120f, 336f, 200f, 900f))
        assertEquals(CapsuleDock.Center, nearestCapsuleDock(120f, 336f, 200f, -900f))
        assertEquals(CapsuleDock.Left, nearestCapsuleDock(68f, 336f, 200f, -900f))
        assertEquals(CapsuleDock.Left, nearestCapsuleDock(10f, 336f, 200f, -900f))
    }
}
