package com.tokidiscover.app.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    if (useDynamic && supportsDynamicColor) {
        val context = LocalContext.current
        return if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    return schemeFromSeed(seed, dark)
}
