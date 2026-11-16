package com.tokidiscover.app.ui

/**
 * How far content has slid under the large title.
 * A downward finger drag (pulling content back to the top) has a positive y delta.
 * The offset stays ≤ 0; more negative means more content is behind the title.
 */
internal fun headerScrollOffset(current: Float, consumedY: Float, availableY: Float): Float {
    val next = if (consumedY == 0f && availableY > 0f) 0f else current + consumedY
    return next.coerceAtMost(0f)
}

internal fun headerOverlapsContent(offset: Float, thresholdPx: Float): Boolean = offset < -thresholdPx

/** Scroll-down collapses the floating address bar; scroll-up or editing opens it again. */
internal fun nextOmniboxCollapsed(collapsed: Boolean, scrollDeltaY: Int, editing: Boolean): Boolean {
    if (editing) return false
    if (scrollDeltaY > 12) return true
    if (scrollDeltaY < -12) return false
    return collapsed
}
