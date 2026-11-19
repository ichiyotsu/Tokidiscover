package com.tokidiscover.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeedDialTest {
    @Test
    fun encodeDecodeRoundTrip() {
        val sites = listOf(
            SpeedDialSite("Bilibili", "https://www.bilibili.com"),
            SpeedDialSite("自定", "https://example.com/a"),
        )
        assertEquals(sites, decodeSpeedDial(encodeSpeedDial(sites)))
    }

    @Test
    fun decodeFallsBackToDefaults() {
        assertEquals(DefaultSpeedDial, decodeSpeedDial(""))
        assertEquals(DefaultSpeedDial, decodeSpeedDial("not json"))
    }

    @Test
    fun decodeKeepsExplicitlyEmptyList() {
        // Deleting every site stores "[]"; it must stay empty after restart,
        // not resurrect the defaults.
        assertEquals(emptyList<SpeedDialSite>(), decodeSpeedDial("[]"))
        assertEquals(emptyList<SpeedDialSite>(), decodeSpeedDial(encodeSpeedDial(emptyList())))
    }

    @Test
    fun decodeDropsBlankEntries() {
        val decoded = decodeSpeedDial(
            "[{\"title\":\"好的\",\"url\":\"https://ok.example\"},{\"title\":\"\",\"url\":\"https://blank.example\"}]",
        )
        assertEquals(listOf(SpeedDialSite("好的", "https://ok.example")), decoded)
    }
}
