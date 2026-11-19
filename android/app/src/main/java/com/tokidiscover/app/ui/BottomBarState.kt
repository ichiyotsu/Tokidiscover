package com.tokidiscover.app.ui

/**
 * The two shapes of the bottom bar (TokiUI 0.1.11):
 * - [Nav]: the regular floating glass capsule nav, on the home page and
 *   the app destinations (tabs, library, settings).
 * - [FunctionBar]: on a website — the URL/function bar, always. Outer
 *   glass capsule, inner pill with the full URL; the page actions live
 *   in the bar. No scroll-collapse, no domain capsule: the bar stays.
 */
enum class BottomBarState { Nav, FunctionBar }

/** Which bottom-bar shape to show: the function bar on a website, the nav capsule elsewhere. */
fun bottomBarState(showingPage: Boolean): BottomBarState =
    if (showingPage) BottomBarState.FunctionBar else BottomBarState.Nav
