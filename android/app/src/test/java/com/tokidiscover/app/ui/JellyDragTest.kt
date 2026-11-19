package com.tokidiscover.app.ui

import com.tokidiscover.app.ui.components.jellyDeformation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JellyDragTest {
    @Test
    fun noOffsetMeansNoDeformation() {
        val d = jellyDeformation(0f, 0f)
        assertEquals(1f, d.scaleX, 0.001f)
        assertEquals(1f, d.scaleY, 0.001f)
        assertEquals(0f, d.rotationDegrees, 0.001f)
    }

    @Test
    fun horizontalDragStretchesAlongXAndTilts() {
        val d = jellyDeformation(120f, 0f)
        assertTrue(d.scaleX > 1f)
        assertTrue(d.scaleY < 1f)
        assertTrue(d.rotationDegrees > 0f)
    }

    @Test
    fun oppositeDragMirrorsTheTilt() {
        val right = jellyDeformation(120f, 0f)
        val left = jellyDeformation(-120f, 0f)
        assertEquals(right.scaleX, left.scaleX, 0.001f)
        assertEquals(-right.rotationDegrees, left.rotationDegrees, 0.001f)
    }

    @Test
    fun verticalDragStretchesAlongY() {
        val d = jellyDeformation(0f, 120f)
        assertTrue(d.scaleY > 1f)
        assertTrue(d.scaleX < 1f)
    }

    @Test
    fun deformationStaysBounded() {
        val d = jellyDeformation(10_000f, -10_000f)
        assertTrue(d.scaleX in 0.9f..1.1f)
        assertTrue(d.scaleY in 0.9f..1.1f)
        assertTrue(kotlin.math.abs(d.rotationDegrees) <= 4.001f)
    }
}
