package com.tokidiscover.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChromeStrokeTest {
    @Test
    fun solidBarStrokeIsThreeDpWithASixDpGap() {
        assertEquals(3f, TokiChromeStroke.width.value)
        assertEquals(6f, TokiChromeStroke.barGap.value)
    }

    @Test
    fun lightFrostIsAWarmVeilThenWhiteAndDarkFrostIsWhiteOnly() {
        assertEquals(2, TokiGlassParams.tints(dark = false).size)
        assertEquals(1, TokiGlassParams.tints(dark = true).size)
    }

    @Test
    fun glassOffUsesTheSolidChrome() {
        assertTrue(solidChrome(backdropBlur = false))
    }
}
