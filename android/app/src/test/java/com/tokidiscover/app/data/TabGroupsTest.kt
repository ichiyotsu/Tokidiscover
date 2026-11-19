package com.tokidiscover.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TabGroupsTest {
    private val t0 = 1_700_000_000_000L

    @Test
    fun createGroupStealsTabsFromOtherGroups() {
        val (first, g1) = createTabGroup(emptyList(), "A", listOf("t1", "t2"), t0)
        val (second, g2) = createTabGroup(first, "B", listOf("t2", "t3"), t0)
        assertEquals(listOf("t1"), second.first { it.id == g1.id }.tabIds)
        assertEquals(listOf("t2", "t3"), second.first { it.id == g2.id }.tabIds)
        assertEquals(2, second.size)
    }

    @Test
    fun createGroupWithBlankNameGetsANumber() {
        val (_, g) = createTabGroup(emptyList(), "  ", listOf("t1"), t0)
        assertEquals("群组 1", g.name)
    }

    @Test
    fun addTabMovesItBetweenGroupsAndDropsEmptyOnes() {
        val (groups, g1) = createTabGroup(emptyList(), "A", listOf("t1"), t0)
        val (groups2, g2) = createTabGroup(groups, "B", listOf("t2"), t0)
        val moved = addTabToGroup(groups2, g2.id, "t1", t0)
        assertTrue(moved.none { it.id == g1.id })
        assertEquals(listOf("t2", "t1"), moved.first { it.id == g2.id }.tabIds)
    }

    @Test
    fun removeLastTabDropsTheGroup() {
        val (groups, g) = createTabGroup(emptyList(), "A", listOf("t1", "t2"), t0)
        val rest = removeTabFromGroup(groups, g.id, "t1", t0)
        assertEquals(listOf("t2"), rest.first().tabIds)
        assertTrue(removeTabFromGroup(rest, g.id, "t2", t0).isEmpty())
    }

    @Test
    fun renameKeepsTabsAndIgnoresBlank() {
        val (groups, g) = createTabGroup(emptyList(), "A", listOf("t1"), t0)
        val renamed = renameTabGroup(groups, g.id, "工作", t0)
        assertEquals("工作", renamed.first().name)
        assertEquals(listOf("t1"), renamed.first().tabIds)
        assertEquals("工作", renameTabGroup(renamed, g.id, "  ", t0).first().name)
    }

    @Test
    fun deleteGroupKeepsItsTabsUngrouped() {
        val (groups, g) = createTabGroup(emptyList(), "A", listOf("t1", "t2"), t0)
        val rest = deleteTabGroup(groups, g.id)
        assertTrue(rest.isEmpty())
        assertEquals(listOf("t1", "t2"), ungroupedTabIds(listOf("t1", "t2"), rest))
        assertEquals(g, groupOfTab(groups, "t1"))
    }

    @Test
    fun closeGroupReturnsItsTabIds() {
        val (groups, g) = createTabGroup(emptyList(), "A", listOf("t1"), t0)
        val (rest, ids) = closeTabGroup(groups, g.id)
        assertTrue(rest.isEmpty())
        assertEquals(listOf("t1"), ids)
    }

    @Test
    fun lastUpdatedFormatsRelatively() {
        assertEquals("刚才", formatLastUpdated(t0, t0 - 30_000L))
        assertEquals("5 分钟前", formatLastUpdated(t0, t0 - 5 * 60_000L))
        assertEquals("3 小时前", formatLastUpdated(t0, t0 - 3 * 3_600_000L))
        val day = formatLastUpdated(t0, t0 - 25 * 3_600_000L)
        assertTrue(day.contains("月") && day.contains("日"))
    }

    @Test
    fun encodeDecodeRoundTripsGroupsAndSnapshots() {
        val (groups, _) = createTabGroup(emptyList(), "工作", listOf("t1", "t2"), t0)
        val snaps = listOf(
            TabSnapshot("t1", "Bilibili", "https://www.bilibili.com", false),
            TabSnapshot("t2", "GitHub", "https://github.com", false),
            TabSnapshot("t9", "Unused", "https://example.com", false),
        )
        val json = encodeTabGroups(groups, snaps)
        val (decoded, decodedSnaps) = decodeTabGroups(json)
        assertEquals(1, decoded.size)
        assertEquals("工作", decoded.first().name)
        assertEquals(listOf("t1", "t2"), decoded.first().tabIds)
        assertEquals(2, decodedSnaps.size) // t9 is not in any group
        assertEquals("https://github.com", decodedSnaps.first { it.tabId == "t2" }.url)
    }

    @Test
    fun decodeBadInputYieldsNothing() {
        assertEquals(emptyList<TabGroup>() to emptyList<TabSnapshot>(), decodeTabGroups("not json"))
        assertEquals(emptyList<TabGroup>() to emptyList<TabSnapshot>(), decodeTabGroups(""))
    }

    @Test
    fun recentlyClosedCapsAtTenAndDedupesByUrl() {
        var closed = emptyList<ClosedTab>()
        repeat(12) { i -> closed = recordClosedTab(closed, ClosedTab("T$i", "https://example.com/$i", false)) }
        assertEquals(10, closed.size)
        assertEquals("https://example.com/11", closed.first().url)
        closed = recordClosedTab(closed, ClosedTab("T5 again", "https://example.com/5", false))
        assertEquals(10, closed.size)
        assertEquals("https://example.com/5", closed.first().url)
        assertEquals(1, closed.count { it.url == "https://example.com/5" })
    }
}
