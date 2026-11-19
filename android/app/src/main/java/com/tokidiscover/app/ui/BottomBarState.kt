package com.tokidiscover.app.ui

/**
 * The three shapes of the bottom bar (TokiStudio 0.1.10 redesign):
 * - [Nav]: the regular floating glass capsule nav, on the home page and
 *   the app destinations (tabs, library, settings).
 * - [FunctionBar]: on a website, the URL/function bar — outer glass
 *   capsule, inner pill with the full URL.
 * - [Capsule]: while scrolling a website, the function bar shrinks to a
 *   draggable capsule showing only the main domain.
 */
enum class BottomBarState { Nav, FunctionBar, Capsule }

/** Which bottom-bar shape to show. Editing always opens the function bar. */
fun bottomBarState(showingPage: Boolean, collapsed: Boolean, editing: Boolean): BottomBarState = when {
    editing && showingPage -> BottomBarState.FunctionBar
    !showingPage -> BottomBarState.Nav
    collapsed -> BottomBarState.Capsule
    else -> BottomBarState.FunctionBar
}

/** Main domain shown in the collapsed capsule: the URL host without a leading "www.". */
fun mainDomain(url: String): String {
    val withoutScheme = url.substringAfter("://", url)
    val host = withoutScheme.substringBefore('/').substringBefore('?').substringBefore('#')
        .substringAfterLast('@')
    return host.removePrefix("www.")
}
