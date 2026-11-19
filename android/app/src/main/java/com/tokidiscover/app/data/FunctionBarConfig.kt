package com.tokidiscover.app.data

import org.json.JSONArray

/**
 * One independent component of the bottom function bar (TokiUI 0.1.11).
 * Every button counts as one component; the bar holds at most
 * [MAX_FUNCTION_BAR_ITEMS] of them, search pill included. Settings can
 * never be removed, but it can be moved like any other component.
 */
enum class FunctionBarComponent {
    Search, Back, Forward, Refresh, Tabs, Bookmark, Share, Find, Home, Settings
}

fun FunctionBarComponent.label(): String = when (this) {
    FunctionBarComponent.Search -> "搜索框"
    FunctionBarComponent.Back -> "后退"
    FunctionBarComponent.Forward -> "前进"
    FunctionBarComponent.Refresh -> "刷新"
    FunctionBarComponent.Tabs -> "标签页"
    FunctionBarComponent.Bookmark -> "收藏"
    FunctionBarComponent.Share -> "分享"
    FunctionBarComponent.Find -> "页内查找"
    FunctionBarComponent.Home -> "主页"
    FunctionBarComponent.Settings -> "设置"
}

/** Max components on the function bar, search pill included. */
const val MAX_FUNCTION_BAR_ITEMS = 7

/** Default bar: search, back, forward, refresh, tabs, settings. Tabs sits right left of Settings. */
val DEFAULT_FUNCTION_BAR_ITEMS = listOf(
    FunctionBarComponent.Search,
    FunctionBarComponent.Back,
    FunctionBarComponent.Forward,
    FunctionBarComponent.Refresh,
    FunctionBarComponent.Tabs,
    FunctionBarComponent.Settings,
)

/**
 * Normalize a stored/custom list: dedupe (first wins), always keep
 * Settings, cap at [MAX_FUNCTION_BAR_ITEMS]. When over the cap, non-Settings
 * items are dropped from the end — Settings is never dropped.
 */
fun sanitizeFunctionBarItems(items: List<FunctionBarComponent>): List<FunctionBarComponent> {
    val distinct = items.distinct()
    val withSettings =
        if (FunctionBarComponent.Settings in distinct) distinct else distinct + FunctionBarComponent.Settings
    if (withSettings.size <= MAX_FUNCTION_BAR_ITEMS) return withSettings
    val kept = withSettings.filter { it != FunctionBarComponent.Settings }.take(MAX_FUNCTION_BAR_ITEMS - 1)
    return kept + FunctionBarComponent.Settings
}

/** Move one item from [from] to [to] (clamped); no-op on bad indices. Pure, so drag + settings share it. */
fun moveFunctionBarItem(
    items: List<FunctionBarComponent>,
    from: Int,
    to: Int,
): List<FunctionBarComponent> {
    if (from !in items.indices) return items
    val target = to.coerceIn(0, items.lastIndex)
    if (target == from) return items
    val mutable = items.toMutableList()
    val item = mutable.removeAt(from)
    mutable.add(target, item)
    return mutable
}

fun encodeFunctionBarItems(items: List<FunctionBarComponent>): String =
    JSONArray(items.map { it.name }).toString()

/** Decode a stored bar layout; blank / invalid / empty falls back to the default. */
fun decodeFunctionBarItems(json: String): List<FunctionBarComponent> {
    if (json.isBlank()) return DEFAULT_FUNCTION_BAR_ITEMS
    return runCatching {
        val array = JSONArray(json)
        val items = List(array.length()) { i ->
            runCatching { FunctionBarComponent.valueOf(array.getString(i)) }.getOrNull()
        }.filterNotNull()
        if (items.isEmpty()) DEFAULT_FUNCTION_BAR_ITEMS else sanitizeFunctionBarItems(items)
    }.getOrDefault(DEFAULT_FUNCTION_BAR_ITEMS)
}
