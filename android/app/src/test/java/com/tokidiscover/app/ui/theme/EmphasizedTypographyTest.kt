package com.tokidiscover.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Test

class EmphasizedTypographyTest {
    @Test
    fun allFifteenEmphasizedStylesAreBold() {
        val type = Typography().withTokiType(InterFamily)
        listOf(
            type.displayLargeEmphasized, type.displayMediumEmphasized, type.displaySmallEmphasized,
            type.headlineLargeEmphasized, type.headlineMediumEmphasized, type.headlineSmallEmphasized,
            type.titleLargeEmphasized, type.titleMediumEmphasized, type.titleSmallEmphasized,
            type.bodyLargeEmphasized, type.bodyMediumEmphasized, type.bodySmallEmphasized,
            type.labelLargeEmphasized, type.labelMediumEmphasized, type.labelSmallEmphasized,
        ).forEach { assertEquals(FontWeight.Bold, it.fontWeight) }
    }

    @Test
    fun displayHeadlineTitleAndLabelStayBoldWhileBodyStaysRegular() {
        val type = Typography().withTokiType(InterFamily)
        listOf(type.displayLarge, type.headlineMedium, type.titleMedium, type.labelLarge)
            .forEach { assertEquals(FontWeight.Bold, it.fontWeight) }
        listOf(type.bodyLarge, type.bodyMedium, type.bodySmall)
            .forEach { assertEquals(FontWeight.Normal, it.fontWeight) }
    }
}
