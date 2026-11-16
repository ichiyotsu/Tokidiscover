package com.tokidiscover.app.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeFeatureFlags
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

/**
 * Window sampling for the floating capsules.
 *
 * [androidx.compose.ui.draw.blur] and [HazeInput.Content] only blur the control's
 * own pixels, so they are not used here. [HazeInput.Backdrop] samples pixels
 * already drawn in this window and falls back to the `hazeSource` content root.
 */
@OptIn(ExperimentalHazeApi::class)
object TokiBlurCapability {
    fun install() {
        HazeFeatureFlags.isPlatformBackdropEnabled = true
    }

    val canSampleInWindow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

object TokiGlassParams {
    val blurRadius: Dp = 24.dp
    const val noise: Float = 0.02f

    fun frost(dark: Boolean): Color = Color.White.copy(alpha = if (dark) 0.20f else 0.40f)

    fun fallback(dark: Boolean): Color = Color.Black.copy(alpha = if (dark) 0.45f else 0.30f)
}

@OptIn(ExperimentalHazeApi::class)
private fun tokiBlurStyle(dark: Boolean): HazeBlurStyle = HazeBlurStyle {
    blurEnabled(true)
    blurRadius(TokiGlassParams.blurRadius)
    noiseFactor(TokiGlassParams.noise)
    backgroundColor(Color.Transparent)
    colorEffects(listOf(HazeColorEffect.tint(TokiGlassParams.frost(dark))))
    fallbackColorEffect(HazeColorEffect.tint(TokiGlassParams.fallback(dark)))
}

@OptIn(ExperimentalHazeApi::class)
@Composable
fun Modifier.tokiGlass(state: HazeState): Modifier {
    val dark = LocalDarkTheme.current
    return hazeBlur(
        input = HazeInput.Backdrop(state),
        style = tokiBlurStyle(dark),
        performanceMode = HazePerformanceMode.Quality,
    )
}

/** Glass inside a capsule when blur is on and the device can sample; otherwise an opaque surface. */
@Composable
fun tokiChrome(state: HazeState, backdropBlur: Boolean): Modifier {
    return if (backdropBlur && TokiBlurCapability.canSampleInWindow) {
        Modifier.tokiGlass(state)
    } else {
        Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
    }
}
