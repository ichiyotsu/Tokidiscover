package com.tokidiscover.app.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
 * Window sampling for floating capsules and scroll chips.
 *
 * [androidx.compose.ui.draw.blur] and [HazeInput.Content] only blur the control's
 * own pixels, so they are not used. [HazeInput.Backdrop] samples pixels already
 * drawn in this window and falls back to the `hazeSource` content root.
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

    /** Light: 16% warm veil, then 46% white. Dark: 20% white, no black veil. */
    fun tints(dark: Boolean): List<HazeColorEffect> = if (dark) {
        listOf(HazeColorEffect.tint(Color.White.copy(alpha = 0.20f)))
    } else {
        listOf(
            HazeColorEffect.tint(Color(0xFF2A2622).copy(alpha = 0.16f)),
            HazeColorEffect.tint(Color.White.copy(alpha = 0.46f)),
        )
    }

    fun fallback(dark: Boolean): Color = Color.Black.copy(alpha = if (dark) 0.45f else 0.30f)
}

/** Solid bar and scroll-chip stroke. Glass surfaces do not use this stroke. */
object TokiChromeStroke {
    val width: Dp = 3.dp
    val barGap: Dp = 6.dp
}

@OptIn(ExperimentalHazeApi::class)
private fun tokiBlurStyle(dark: Boolean): HazeBlurStyle = HazeBlurStyle {
    blurEnabled(true)
    blurRadius(TokiGlassParams.blurRadius)
    noiseFactor(TokiGlassParams.noise)
    backgroundColor(Color.Transparent)
    colorEffects(TokiGlassParams.tints(dark))
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

fun solidChrome(backdropBlur: Boolean): Boolean =
    !(backdropBlur && TokiBlurCapability.canSampleInWindow)

/**
 * Scroll chip behind a large title or back button.
 * Frosted chips are only the shared blur layer. Solid chips are surfaceContainer plus the 3dp primary stroke.
 */
@Composable
fun TokiScrollChipBackdrop(
    shape: Shape,
    cover: Float,
    backdropBlur: Boolean,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    applyBlur: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val frosted = backdropBlur && TokiBlurCapability.canSampleInWindow
    val canBlur = applyBlur && hazeState != null && cover > 0.01f && frosted
    Box(
        modifier
            .clip(shape)
            .then(if (canBlur) Modifier.tokiGlass(hazeState) else Modifier)
            .then(
                if (!frosted && cover > 0f) {
                    Modifier
                        .background(scheme.surfaceContainer.copy(alpha = cover))
                        .border(TokiChromeStroke.width, scheme.primary.copy(alpha = cover), shape)
                } else {
                    Modifier
                },
            ),
    )
}
