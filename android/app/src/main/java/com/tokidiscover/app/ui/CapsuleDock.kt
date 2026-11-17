package com.tokidiscover.app.ui

/** The three fixed dock positions of the scroll-shrunk omnibox capsule. */
enum class CapsuleDock { Left, Center, Right }

/** Left-edge pixel offset of the capsule for [dock] inside a container of [containerWidthPx]. */
fun capsuleOffsetFor(dock: CapsuleDock, containerWidthPx: Float, capsuleWidthPx: Float): Float {
    val travel = (containerWidthPx - capsuleWidthPx).coerceAtLeast(0f)
    return when (dock) {
        CapsuleDock.Left -> 0f
        CapsuleDock.Center -> travel / 2f
        CapsuleDock.Right -> travel
    }
}

/**
 * Which dock a released drag settles on. A fast fling moves one dock in the
 * fling direction; otherwise the capsule snaps to the nearest dock (the
 * boundaries are the midpoints between neighbouring docks).
 */
fun nearestCapsuleDock(
    offsetPx: Float,
    containerWidthPx: Float,
    capsuleWidthPx: Float,
    velocityPxPerSec: Float,
): CapsuleDock {
    val travel = (containerWidthPx - capsuleWidthPx).coerceAtLeast(0f)
    if (travel <= 0f) return CapsuleDock.Center
    val nearest = when {
        offsetPx < travel / 4f -> CapsuleDock.Left
        offsetPx > travel * 3f / 4f -> CapsuleDock.Right
        else -> CapsuleDock.Center
    }
    val flingThreshold = 400f
    return when {
        velocityPxPerSec > flingThreshold -> when (nearest) {
            CapsuleDock.Left -> CapsuleDock.Center
            else -> CapsuleDock.Right
        }
        velocityPxPerSec < -flingThreshold -> when (nearest) {
            CapsuleDock.Right -> CapsuleDock.Center
            else -> CapsuleDock.Left
        }
        else -> nearest
    }
}
