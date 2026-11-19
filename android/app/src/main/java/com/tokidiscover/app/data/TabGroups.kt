package com.tokidiscover.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Tab groups (Chrome-style). Pure logic — no Android types, so unit tests
 * stay JVM-only. A tab belongs to at most one group; a group that loses its
 * last tab disappears, like Chrome.
 */
data class TabGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorIndex: Int = 0,
    val tabIds: List<String> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/** A tab frozen for persistence: groups (and their tabs) survive a restart, plain tabs do not. */
data class TabSnapshot(val tabId: String, val title: String, val url: String, val isPrivate: Boolean)

/** A tab the user just closed, restorable from the tabs page. */
data class ClosedTab(
    val title: String,
    val url: String,
    val isPrivate: Boolean,
    val closedAt: Long = System.currentTimeMillis(),
)

/** Chrome-style group colors, as ARGB ints; the UI wraps them in Color(). */
val TabGroupColors = listOf(
    0xFF7FB069.toInt(), // green
    0xFF6A8DFF.toInt(), // blue
    0xFFE8A33D.toInt(), // orange
    0xFF9B7EDE.toInt(), // purple
    0xFFE06C5B.toInt(), // red
    0xFF4DB6AC.toInt(), // teal
)

/** Create a group holding [tabIds]; the tabs are stolen from any other group. */
fun createTabGroup(
    groups: List<TabGroup>,
    name: String,
    tabIds: List<String>,
    now: Long = System.currentTimeMillis(),
): Pair<List<TabGroup>, TabGroup> {
    val wanted = tabIds.distinct()
    val group = TabGroup(
        name = name.ifBlank { "群组 ${groups.size + 1}" },
        colorIndex = groups.size % TabGroupColors.size,
        tabIds = wanted,
        updatedAt = now,
    )
    val rest = groups.map { it.copy(tabIds = it.tabIds - wanted.toSet(), updatedAt = now) }
        .filter { it.tabIds.isNotEmpty() }
    return (rest + group) to group
}

/** Add [tabId] to [groupId], stealing it from any other group. No-op when the group is missing. */
fun addTabToGroup(
    groups: List<TabGroup>,
    groupId: String,
    tabId: String,
    now: Long = System.currentTimeMillis(),
): List<TabGroup> {
    if (groups.none { it.id == groupId }) return groups
    return groups.map { group ->
        when {
            group.id == groupId && tabId !in group.tabIds ->
                group.copy(tabIds = group.tabIds + tabId, updatedAt = now)
            group.id != groupId && tabId in group.tabIds ->
                group.copy(tabIds = group.tabIds - tabId, updatedAt = now)
            else -> group
        }
    }.filter { it.tabIds.isNotEmpty() }
}

/** Remove [tabId] from [groupId]; the group disappears when it becomes empty. */
fun removeTabFromGroup(
    groups: List<TabGroup>,
    groupId: String,
    tabId: String,
    now: Long = System.currentTimeMillis(),
): List<TabGroup> = groups.mapNotNull { group ->
    if (group.id != groupId) return@mapNotNull group
    val rest = group.tabIds - tabId
    if (rest.isEmpty()) null else group.copy(tabIds = rest, updatedAt = now)
}

fun renameTabGroup(
    groups: List<TabGroup>,
    groupId: String,
    name: String,
    now: Long = System.currentTimeMillis(),
): List<TabGroup> = groups.map {
    if (it.id == groupId) it.copy(name = name.ifBlank { it.name }, updatedAt = now) else it
}

fun deleteTabGroup(groups: List<TabGroup>, groupId: String): List<TabGroup> =
    groups.filterNot { it.id == groupId }

/** Close a whole group: returns the remaining groups and the tab ids to close. */
fun closeTabGroup(groups: List<TabGroup>, groupId: String): Pair<List<TabGroup>, List<String>> {
    val group = groups.firstOrNull { it.id == groupId } ?: return groups to emptyList()
    return groups.filterNot { it.id == groupId } to group.tabIds
}

fun groupOfTab(groups: List<TabGroup>, tabId: String): TabGroup? =
    groups.firstOrNull { tabId in it.tabIds }

fun ungroupedTabIds(allTabIds: List<String>, groups: List<TabGroup>): List<String> {
    val grouped = groups.flatMap { it.tabIds }.toSet()
    return allTabIds.filterNot { it in grouped }
}

/** Chrome-style relative time: 刚才 / N 分钟前 / N 小时前, then the month and day. */
fun formatLastUpdated(now: Long, updatedAt: Long): String {
    val diff = (now - updatedAt).coerceAtLeast(0L)
    return when {
        diff < 60_000L -> "刚才"
        diff < 3_600_000L -> "${diff / 60_000L} 分钟前"
        diff < 86_400_000L -> "${diff / 3_600_000L} 小时前"
        else -> SimpleDateFormat("M月d日", Locale.getDefault()).format(Date(updatedAt))
    }
}

/** Record a closed tab for the tabs page: newest first, one entry per URL, capped at [max]. */
fun recordClosedTab(closed: List<ClosedTab>, tab: ClosedTab, max: Int = 10): List<ClosedTab> =
    (listOf(tab) + closed.filterNot { it.url == tab.url }).take(max)

/** Encode groups + the snapshots of their tabs as one JSON string for BrowserStore. */
fun encodeTabGroups(groups: List<TabGroup>, snapshots: List<TabSnapshot>): String {
    val byId = snapshots.associateBy { it.tabId }
    val root = JSONObject()
    val array = JSONArray()
    groups.forEach { group ->
        val obj = JSONObject()
            .put("id", group.id)
            .put("name", group.name)
            .put("colorIndex", group.colorIndex)
            .put("updatedAt", group.updatedAt)
        val tabs = JSONArray()
        group.tabIds.mapNotNull { byId[it] }.forEach { snap ->
            tabs.put(
                JSONObject()
                    .put("id", snap.tabId)
                    .put("title", snap.title)
                    .put("url", snap.url)
                    .put("isPrivate", snap.isPrivate),
            )
        }
        obj.put("tabs", tabs)
        array.put(obj)
    }
    root.put("groups", array)
    return root.toString()
}

/** Decode what [encodeTabGroups] wrote. Bad input yields no groups, never a crash. */
fun decodeTabGroups(json: String): Pair<List<TabGroup>, List<TabSnapshot>> {
    return runCatching {
        val root = JSONObject(json)
        val array = root.optJSONArray("groups") ?: return emptyList<TabGroup>() to emptyList()
        val groups = mutableListOf<TabGroup>()
        val snapshots = mutableListOf<TabSnapshot>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val tabIds = mutableListOf<String>()
            val tabs = obj.optJSONArray("tabs") ?: JSONArray()
            for (j in 0 until tabs.length()) {
                val t = tabs.getJSONObject(j)
                val snap = TabSnapshot(
                    tabId = t.getString("id"),
                    title = t.optString("title", ""),
                    url = t.optString("url", ""),
                    isPrivate = t.optBoolean("isPrivate", false),
                )
                tabIds.add(snap.tabId)
                snapshots.add(snap)
            }
            groups.add(
                TabGroup(
                    id = obj.getString("id"),
                    name = obj.optString("name", "群组"),
                    colorIndex = obj.optInt("colorIndex", 0),
                    tabIds = tabIds,
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                ),
            )
        }
        groups to snapshots
    }.getOrDefault(emptyList<TabGroup>() to emptyList())
}
