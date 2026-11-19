package com.tokidiscover.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Edge fades + bottom redundancy for the browser content area (TokiUI
 * 0.1.11): page content fades out softly at the top (under the status bar)
 * and at the bottom (above the floating bottom bar) instead of hard
 * clipping, and the WebView viewport ends above the bottom bar so the
 * last line of a page can scroll fully into view.
 *
 * The fades are plain gradient Boxes with no clickable / pointerInput
 * modifiers, so touches pass straight through to the WebView below.
 * They use Brush.verticalGradient over the page-background colors only —
 * no Modifier.blur, no HazeInput.Content (banned for navigation chrome);
 * frosted glass still goes through the existing haze/glass recipe.
 */
object ContentEdge {
    /** Height of the top and bottom fades. */
    val fadeHeight: Dp = 32.dp

    /**
     * Resting bottom-bar height, system navigation inset included: the
     * function bar is 72dp and the shell pads it by the navigation inset
     * (~16dp on gesture nav) + 8dp, so ~96dp in the common case.
     */
    val bottomBarHeight: Dp = 96.dp

    /** Extra clearance between the end of the page and the top of the bar. */
    val bottomRedundancy: Dp = 24.dp

    /**
     * Fixed bottom inset for the WebView viewport: bar height + redundancy.
     * A fixed value on purpose — it is set once and never follows the
     * measured bar height, so the WebView never resizes while navigating
     * or scrolling (no repaint flicker).
     */
    fun webBottomInset(barHeight: Dp = bottomBarHeight, extra: Dp = bottomRedundancy): Dp =
        barHeight + extra

    /** Gradient stops for an edge fade: opaque edge color fading to transparent. */
    fun fadeColors(edge: Color): List<Color> = listOf(edge, edge.copy(alpha = 0f))
}

/**
 * Top + bottom edge fades for a web page. Place inside the content Box,
 * above the WebView. [bottomInset] must be [ContentEdge.webBottomInset]
 * so the bottom fade sits exactly on the WebView's bottom edge, with the
 * redundancy gap below it.
 *
 * Edge colors follow the page background gradient
 * (see tokiPageBackground): surfaceContainer at the top, background at
 * the bottom — so the fades melt into the app chrome in both modes.
 */
@Composable
fun ContentEdgeFades(
    bottomInset: Dp,
    modifier: Modifier = Modifier,
    topInset: Dp = 0.dp,
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier) {
        // Top: opaque surfaceContainer fading down to transparent. topInset
        // pushes the fade below a fixed header (0 on a web page).
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = topInset)
                .height(ContentEdge.fadeHeight)
                .background(
                    Brush.verticalGradient(ContentEdge.fadeColors(scheme.surfaceContainer)),
                ),
        )
        // Bottom: transparent fading up to opaque background, sitting on
        // the WebView's bottom edge (bottomInset below it is clean page
        // background, then the floating bar).
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = bottomInset)
                .height(ContentEdge.fadeHeight)
                .background(
                    Brush.verticalGradient(ContentEdge.fadeColors(scheme.background).reversed()),
                ),
        )
    }
}
