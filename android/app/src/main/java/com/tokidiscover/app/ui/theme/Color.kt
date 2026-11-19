package com.tokidiscover.app.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.dynamiccolor.MaterialDynamicColors
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.SchemeTonalSpot

/**
 * Runtime colors come from dynamic color or from a TokiUI seed run through
 * Material Color Utilities (SchemeTonalSpot). Seed hex lives only in
 * `tokiui.semantic.tokens.json`; this file does not copy a palette.
 */
fun colorFromHex(hex: String): Color {
    val rgb = hex.removePrefix("#").toLong(16)
    return Color((0xFF000000L or rgb).toInt())
}

fun schemeFromSeed(seed: Color, dark: Boolean): ColorScheme {
    val scheme = SchemeTonalSpot(Hct.fromInt(seed.toArgb()), dark, 0.0)
    val m = MaterialDynamicColors()
    fun c(get: MaterialDynamicColors.() -> com.materialkolor.dynamiccolor.DynamicColor): Color =
        Color(m.get().getArgb(scheme))
    return if (dark) {
        darkColorScheme(
            primary = c { primary() }, onPrimary = c { onPrimary() },
            primaryContainer = c { primaryContainer() }, onPrimaryContainer = c { onPrimaryContainer() },
            inversePrimary = c { inversePrimary() },
            secondary = c { secondary() }, onSecondary = c { onSecondary() },
            secondaryContainer = c { secondaryContainer() }, onSecondaryContainer = c { onSecondaryContainer() },
            tertiary = c { tertiary() }, onTertiary = c { onTertiary() },
            tertiaryContainer = c { tertiaryContainer() }, onTertiaryContainer = c { onTertiaryContainer() },
            background = c { background() }, onBackground = c { onBackground() },
            surface = c { surface() }, onSurface = c { onSurface() },
            surfaceVariant = c { surfaceVariant() }, onSurfaceVariant = c { onSurfaceVariant() },
            surfaceDim = c { surfaceDim() }, surfaceBright = c { surfaceBright() },
            surfaceContainerLowest = c { surfaceContainerLowest() }, surfaceContainerLow = c { surfaceContainerLow() },
            surfaceContainer = c { surfaceContainer() }, surfaceContainerHigh = c { surfaceContainerHigh() },
            surfaceContainerHighest = c { surfaceContainerHighest() },
            inverseSurface = c { inverseSurface() }, inverseOnSurface = c { inverseOnSurface() },
            error = c { error() }, onError = c { onError() },
            errorContainer = c { errorContainer() }, onErrorContainer = c { onErrorContainer() },
            outline = c { outline() }, outlineVariant = c { outlineVariant() }, scrim = c { scrim() },
            surfaceTint = c { surfaceTint() },
            primaryFixed = c { primaryFixed() }, primaryFixedDim = c { primaryFixedDim() },
            onPrimaryFixed = c { onPrimaryFixed() }, onPrimaryFixedVariant = c { onPrimaryFixedVariant() },
            secondaryFixed = c { secondaryFixed() }, secondaryFixedDim = c { secondaryFixedDim() },
            onSecondaryFixed = c { onSecondaryFixed() }, onSecondaryFixedVariant = c { onSecondaryFixedVariant() },
            tertiaryFixed = c { tertiaryFixed() }, tertiaryFixedDim = c { tertiaryFixedDim() },
            onTertiaryFixed = c { onTertiaryFixed() }, onTertiaryFixedVariant = c { onTertiaryFixedVariant() },
        )
    } else {
        lightColorScheme(
            primary = c { primary() }, onPrimary = c { onPrimary() },
            primaryContainer = c { primaryContainer() }, onPrimaryContainer = c { onPrimaryContainer() },
            inversePrimary = c { inversePrimary() },
            secondary = c { secondary() }, onSecondary = c { onSecondary() },
            secondaryContainer = c { secondaryContainer() }, onSecondaryContainer = c { onSecondaryContainer() },
            tertiary = c { tertiary() }, onTertiary = c { onTertiary() },
            tertiaryContainer = c { tertiaryContainer() }, onTertiaryContainer = c { onTertiaryContainer() },
            background = c { background() }, onBackground = c { onBackground() },
            surface = c { surface() }, onSurface = c { onSurface() },
            surfaceVariant = c { surfaceVariant() }, onSurfaceVariant = c { onSurfaceVariant() },
            surfaceDim = c { surfaceDim() }, surfaceBright = c { surfaceBright() },
            surfaceContainerLowest = c { surfaceContainerLowest() }, surfaceContainerLow = c { surfaceContainerLow() },
            surfaceContainer = c { surfaceContainer() }, surfaceContainerHigh = c { surfaceContainerHigh() },
            surfaceContainerHighest = c { surfaceContainerHighest() },
            inverseSurface = c { inverseSurface() }, inverseOnSurface = c { inverseOnSurface() },
            error = c { error() }, onError = c { onError() },
            errorContainer = c { errorContainer() }, onErrorContainer = c { onErrorContainer() },
            outline = c { outline() }, outlineVariant = c { outlineVariant() }, scrim = c { scrim() },
            surfaceTint = c { surfaceTint() },
            primaryFixed = c { primaryFixed() }, primaryFixedDim = c { primaryFixedDim() },
            onPrimaryFixed = c { onPrimaryFixed() }, onPrimaryFixedVariant = c { onPrimaryFixedVariant() },
            secondaryFixed = c { secondaryFixed() }, secondaryFixedDim = c { secondaryFixedDim() },
            onSecondaryFixed = c { onSecondaryFixed() }, onSecondaryFixedVariant = c { onSecondaryFixedVariant() },
            tertiaryFixed = c { tertiaryFixed() }, tertiaryFixedDim = c { tertiaryFixedDim() },
            onTertiaryFixed = c { onTertiaryFixed() }, onTertiaryFixedVariant = c { onTertiaryFixedVariant() },
        )
    }
}

val supportsDynamicColor: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun resolveColorScheme(dark: Boolean, useDynamic: Boolean, seed: Color): ColorScheme {
    val base = if (useDynamic && supportsDynamicColor) {
        val context = LocalContext.current
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        schemeFromSeed(seed, dark)
    }
    return if (dark) base.withDarkContrast() else base
}

/**
 * Dark-mode contrast post-processing (TokiStudio 0.1.10): background and
 * surface darken 8%, the surfaceContainer ladder rises from 3% at the
 * lowest rung to 9% at the highest. Only lightness changes; hue and
 * saturation stay as the official scheme made them. Light mode untouched.
 */
internal fun ColorScheme.withDarkContrast(): ColorScheme {
    fun Color.darken(alpha: Float): Color = Color.Black.copy(alpha = alpha).compositeOver(this)
    fun Color.lighten(alpha: Float): Color = Color.White.copy(alpha = alpha).compositeOver(this)
    return copy(
        background = background.darken(0.08f),
        surface = surface.darken(0.08f),
        surfaceDim = surfaceDim.darken(0.08f),
        surfaceContainerLowest = surfaceContainerLowest.lighten(0.03f),
        surfaceContainerLow = surfaceContainerLow.lighten(0.04f),
        surfaceContainer = surfaceContainer.lighten(0.05f),
        surfaceContainerHigh = surfaceContainerHigh.lighten(0.07f),
        surfaceContainerHighest = surfaceContainerHighest.lighten(0.09f),
    )
}

/**
 * Page background (TokiStudio 0.1.10): the vertical surfaceContainer →
 * surface → background ladder, same roles in both modes.
 */
@Composable
fun tokiPageBackground(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.verticalGradient(listOf(scheme.surfaceContainer, scheme.surface, scheme.background))
}
