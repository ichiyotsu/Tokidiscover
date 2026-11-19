package com.tokidiscover.app.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Custom Speed Dial sites, persisted as one JSON string (same pattern as
 * tab_groups). Blank / invalid JSON falls back to [DefaultSpeedDial] — but
 * a valid empty array means the user deleted every site, so it decodes to
 * an empty list (otherwise the defaults would resurrect on restart).
 */
fun encodeSpeedDial(sites: List<SpeedDialSite>): String =
    JSONArray(sites.map { JSONObject().put("title", it.title).put("url", it.url) }).toString()

fun decodeSpeedDial(json: String): List<SpeedDialSite> {
    if (json.isBlank()) return DefaultSpeedDial
    return runCatching {
        val array = JSONArray(json)
        List(array.length()) { i ->
            val o = array.getJSONObject(i)
            SpeedDialSite(o.getString("title"), o.getString("url"))
        }.filter { it.title.isNotBlank() && it.url.isNotBlank() }
    }.getOrDefault(DefaultSpeedDial)
}
