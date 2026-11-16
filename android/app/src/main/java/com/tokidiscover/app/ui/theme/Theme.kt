package com.tokidiscover.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat

enum class ThemeMode { System, Light, Dark }

val LocalDarkTheme = staticCompositionLocalOf { false }

private fun Typography.withTokiType(family: androidx.compose.ui.text.font.FontFamily): Typography {
    fun TextStyle.bold() = copy(fontFamily = family, fontWeight = FontWeight.Bold)
    fun TextStyle.regular() = copy(fontFamily = family, fontWeight = FontWeight.Normal)
    return Typography(
        displayLarge = displayLarge.bold(), displayMedium = displayMedium.bold(), displaySmall = displaySmall.bold(),
        headlineLarge = headlineLarge.bold(), headlineMedium = headlineMedium.bold(), headlineSmall = headlineSmall.bold(),
        titleLarge = titleLarge.bold(), titleMedium = titleMedium.bold(), titleSmall = titleSmall.bold(),
        bodyLarge = bodyLarge.regular(), bodyMedium = bodyMedium.regular(), bodySmall = bodySmall.regular(),
        labelLarge = labelLarge.bold(), labelMedium = labelMedium.bold(), labelSmall = labelSmall.bold(),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TokiDiscoverTheme(
    mode: ThemeMode,
    useDynamicColor: Boolean,
    seed: Color,
    reduceMotion: Boolean,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    CompositionLocalProvider(LocalDarkTheme provides dark) {
        MaterialExpressiveTheme(
            colorScheme = resolveColorScheme(dark, useDynamicColor, seed),
            motionScheme = if (reduceMotion) MotionScheme.standard() else MotionScheme.expressive(),
            typography = Typography().withTokiType(InterFamily),
            content = content,
        )
    }
}
