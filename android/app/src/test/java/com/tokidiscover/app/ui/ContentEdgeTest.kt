package com.tokidiscover.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tokidiscover.app.ui.components.ContentEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentEdgeTest {
    @Test
    fun fadeHeightIsThirtyTwoDp() {
        assertEquals(32.dp, ContentEdge.fadeHeight)
    }

    @Test
    fun webBottomInsetIsBarHeightPlusRedundancy() {
        assertEquals(120.dp, ContentEdge.webBottomInset())
        assertEquals(ContentEdge.bottomBarHeight + ContentEdge.bottomRedundancy, ContentEdge.webBottomInset())
        assertEquals(80.dp, ContentEdge.webBottomInset(barHeight = 56.dp, extra = 24.dp))
    }

    @Test
    fun bottomInsetLeavesRedundancyAboveTheBar() {
        assertTrue(ContentEdge.webBottomInset() > ContentEdge.bottomBarHeight)
        assertEquals(24.dp, ContentEdge.webBottomInset() - ContentEdge.bottomBarHeight)
    }

    @Test
    fun fadeColorsFadeOpaqueEdgeToTransparent() {
        val edge = Color(0xFF6750A4)
        val stops = ContentEdge.fadeColors(edge)
        assertEquals(2, stops.size)
        assertEquals(edge, stops[0])
        assertEquals(0f, stops[1].alpha, 0f)
        assertEquals(edge.red, stops[1].red, 0f)
        assertEquals(edge.green, stops[1].green, 0f)
        assertEquals(edge.blue, stops[1].blue, 0f)
    }
}
