package com.tokidiscover.app.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

class DarkContrastTest {
    private fun luminance(color: Color): Double =
        0.2126 * color.red + 0.7152 * color.green + 0.0722 * color.blue

    @Test
    fun darkContrastDarkensBackgroundAndLiftsContainers() {
        val base = schemeFromSeed(colorFromHex("#6750A4"), dark = true)
        val tuned = base.withDarkContrast()
        assertTrue(luminance(tuned.background) < luminance(base.background))
        assertTrue(luminance(tuned.surface) < luminance(base.surface))
        assertTrue(luminance(tuned.surfaceDim) < luminance(base.surfaceDim))
        assertTrue(luminance(tuned.surfaceContainerLowest) > luminance(base.surfaceContainerLowest))
        assertTrue(luminance(tuned.surfaceContainerLow) > luminance(base.surfaceContainerLow))
        assertTrue(luminance(tuned.surfaceContainer) > luminance(base.surfaceContainer))
        assertTrue(luminance(tuned.surfaceContainerHigh) > luminance(base.surfaceContainerHigh))
        assertTrue(luminance(tuned.surfaceContainerHighest) > luminance(base.surfaceContainerHighest))
    }

    @Test
    fun darkContrastKeepsTheContainerLadderOrdered() {
        val tuned = schemeFromSeed(colorFromHex("#6750A4"), dark = true).withDarkContrast()
        val ladder = listOf(
            tuned.surfaceContainerLowest,
            tuned.surfaceContainerLow,
            tuned.surfaceContainer,
            tuned.surfaceContainerHigh,
            tuned.surfaceContainerHighest,
        ).map(::luminance)
        assertTrue(ladder.zipWithNext().all { (a, b) -> a < b })
    }
}
