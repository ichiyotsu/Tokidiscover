package com.tokidiscover.app.data
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID
/**
 * Tab model. Fields are observable so the omnibox, progress, and back/forward
 * buttons update when the WebView reports them. A plain var on a list item does not.
 * Private tabs never write history.
 */
class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    url: String = UrlNormalizer.NEW_TAB_URL,
    title: String = "新标签页",
    val isPrivate: Boolean = false,
) {
    var url by mutableStateOf(url)
    var title by mutableStateOf(title)
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)
    var progress by mutableStateOf(0)
    /** Site icon from WebChromeClient.onReceivedIcon; null until the page reports one. */
    var favicon by mutableStateOf<Bitmap?>(null)
    /** Page thumbnail captured when the tab was last backgrounded; null until captured. In-memory only. */
    var thumbnail by mutableStateOf<Bitmap?>(null)
}
data class Bookmark(val id: String = UUID.randomUUID().toString(), val title: String, val url: String, val folder: String = "默认", val createdAt: Long = System.currentTimeMillis())
data class HistoryEntry(val id: String = UUID.randomUUID().toString(), val title: String, val url: String, val visitedAt: Long = System.currentTimeMillis())
data class DownloadRecord(val id: String = UUID.randomUUID().toString(), val fileName: String, val url: String, val totalBytes: Long = -1, val status: String = "已开始", val startedAt: Long = System.currentTimeMillis())
data class SpeedDialSite(val title: String, val url: String)
val DefaultSpeedDial = listOf(
    SpeedDialSite("Bilibili", "https://www.bilibili.com"), SpeedDialSite("GitHub", "https://github.com"),
    SpeedDialSite("Wikipedia", "https://www.wikipedia.org"), SpeedDialSite("Bing", "https://www.bing.com"),
    SpeedDialSite("知乎", "https://www.zhihu.com"), SpeedDialSite("YouTube", "https://www.youtube.com"))
