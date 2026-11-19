package com.tokidiscover.app.ui

/**
 * The two shapes of the bottom bar (TokiUI 0.1.11 redesign):
 * - [Nav]: the regular floating glass capsule nav, on the home page and
 *   the app destinations (tabs, library, settings).
 * - [Capsule]: on a website — a draggable capsule showing only the main
 *   domain. The old function bar (URL pill + back/forward/reload/tabs/
 *   menu) is gone: the address field lives in the top omnibox pill and
 *   the page actions live in the right-side toolbar, so the bottom never
 *   repeats function buttons.
 */
enum class BottomBarState { Nav, Capsule }

/** Which bottom-bar shape to show: the domain capsule on a website, the nav capsule elsewhere. */
fun bottomBarState(showingPage: Boolean): BottomBarState =
    if (showingPage) BottomBarState.Capsule else BottomBarState.Nav

/** Main domain shown in the collapsed capsule: the URL host without a leading "www.". */
fun mainDomain(url: String): String {
    val withoutScheme = url.substringAfter("://", url)
    val host = withoutScheme.substringBefore('/').substringBefore('?').substringBefore('#')
        .substringAfterLast('@')
    return host.removePrefix("www.")
}
