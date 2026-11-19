package com.tokidiscover.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FunctionBarConfigTest {
    @Test
    fun defaultBarHasSixItemsWithTabsLeftOfSettings() {
        assertEquals(
            listOf(
                FunctionBarComponent.Search,
                FunctionBarComponent.Back,
                FunctionBarComponent.Forward,
                FunctionBarComponent.Refresh,
                FunctionBarComponent.Tabs,
                FunctionBarComponent.Settings,
            ),
            DEFAULT_FUNCTION_BAR_ITEMS,
        )
    }

    @Test
    fun sanitizeDedupesAndKeepsSettings() {
        val items = listOf(
            FunctionBarComponent.Back,
            FunctionBarComponent.Back,
            FunctionBarComponent.Search,
        )
        assertEquals(
            listOf(
                FunctionBarComponent.Back,
                FunctionBarComponent.Search,
                FunctionBarComponent.Settings,
            ),
            sanitizeFunctionBarItems(items),
        )
    }

    @Test
    fun sanitizeAppendsSettingsWhenMissing() {
        assertEquals(
            listOf(FunctionBarComponent.Search, FunctionBarComponent.Settings),
            sanitizeFunctionBarItems(listOf(FunctionBarComponent.Search)),
        )
    }

    @Test
    fun sanitizeCapsAtSevenWithoutDroppingSettings() {
        val items = listOf(
            FunctionBarComponent.Search,
            FunctionBarComponent.Back,
            FunctionBarComponent.Forward,
            FunctionBarComponent.Refresh,
            FunctionBarComponent.Tabs,
            FunctionBarComponent.Bookmark,
            FunctionBarComponent.Share,
            FunctionBarComponent.Find,
            FunctionBarComponent.Settings,
        )
        val sanitized = sanitizeFunctionBarItems(items)
        assertEquals(MAX_FUNCTION_BAR_ITEMS, sanitized.size)
        assertTrue(FunctionBarComponent.Settings in sanitized)
        assertEquals(FunctionBarComponent.Settings, sanitized.last())
    }

    @Test
    fun moveClampsAndIgnoresBadIndices() {
        val items = listOf(
            FunctionBarComponent.Search,
            FunctionBarComponent.Back,
            FunctionBarComponent.Settings,
        )
        assertEquals(
            listOf(FunctionBarComponent.Back, FunctionBarComponent.Search, FunctionBarComponent.Settings),
            moveFunctionBarItem(items, 0, 1),
        )
        assertEquals(items, moveFunctionBarItem(items, 0, 0))
        assertEquals(items, moveFunctionBarItem(items, 5, 1))
        assertEquals(
            listOf(FunctionBarComponent.Search, FunctionBarComponent.Settings, FunctionBarComponent.Back),
            moveFunctionBarItem(items, 1, 99),
        )
    }

    @Test
    fun encodeDecodeRoundTrip() {
        val items = listOf(
            FunctionBarComponent.Tabs,
            FunctionBarComponent.Search,
            FunctionBarComponent.Settings,
        )
        assertEquals(items, decodeFunctionBarItems(encodeFunctionBarItems(items)))
    }

    @Test
    fun decodeFallsBackToDefault() {
        assertEquals(DEFAULT_FUNCTION_BAR_ITEMS, decodeFunctionBarItems(""))
        assertEquals(DEFAULT_FUNCTION_BAR_ITEMS, decodeFunctionBarItems("not json"))
        assertEquals(DEFAULT_FUNCTION_BAR_ITEMS, decodeFunctionBarItems("[\"Nope\"]"))
        assertEquals(DEFAULT_FUNCTION_BAR_ITEMS, decodeFunctionBarItems("[]"))
    }
}
