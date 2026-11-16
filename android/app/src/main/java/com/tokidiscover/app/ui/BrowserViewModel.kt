package com.tokidiscover.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.tokidiscover.app.TokiDiscoverApplication
import com.tokidiscover.app.data.Bookmark
import com.tokidiscover.app.data.BrowserTab
import com.tokidiscover.app.data.DownloadRecord
import com.tokidiscover.app.data.Downloads
import com.tokidiscover.app.data.NavResult
import com.tokidiscover.app.data.UrlNormalizer

/** Owns the in-memory tab list and library. One WebView per tab is kept by the browser host until the tab closes. */
class BrowserViewModel(app: Application) : AndroidViewModel(app) {
    val settings get() = getApplication<TokiDiscoverApplication>().settings
    val library get() = getApplication<TokiDiscoverApplication>().library
    val tabs = mutableStateListOf<BrowserTab>().apply { add(BrowserTab()) }
    var activeTabId by mutableStateOf(tabs.first().id)
        private set
    val activeTab: BrowserTab get() = tabs.firstOrNull { it.id == activeTabId } ?: tabs.first()
    var bookmarks by mutableStateOf(library.bookmarks)
        private set
    var history by mutableStateOf(library.history)
        private set
    val downloads = mutableStateListOf<DownloadRecord>()
    var pendingNavigation by mutableStateOf<String?>(null)
    var blockedMessage by mutableStateOf<String?>(null)

    fun newTab(isPrivate: Boolean = false, url: String? = null) {
        val tab = BrowserTab(isPrivate = isPrivate)
        tabs.add(tab)
        activeTabId = tab.id
        if (url != null) navigate(url)
    }

    fun closeTab(id: String) {
        val index = tabs.indexOfFirst { it.id == id }
        if (index < 0) return
        tabs.removeAt(index)
        if (tabs.isEmpty()) {
            val tab = BrowserTab()
            tabs.add(tab)
            activeTabId = tab.id
        } else if (activeTabId == id) {
            activeTabId = tabs[minOf(index, tabs.lastIndex)].id
        }
    }

    fun selectTab(id: String) {
        if (tabs.any { it.id == id }) activeTabId = id
    }

    fun submitOmnibox(input: String) {
        when (val result = UrlNormalizer.normalize(input, settings.engine(), settings.customTemplate)) {
            is NavResult.Url -> navigate(result.url)
            is NavResult.Search -> navigate(result.url)
            NavResult.NewTab -> {
                activeTab.url = UrlNormalizer.NEW_TAB_URL
                activeTab.title = "新标签页"
                activeTab.progress = 0
                pendingNavigation = UrlNormalizer.NEW_TAB_URL
            }
            is NavResult.Blocked -> blockedMessage = result.reason
        }
    }

    fun navigate(url: String) {
        if (!UrlNormalizer.isSafeScheme(url) && !url.equals(UrlNormalizer.NEW_TAB_URL, ignoreCase = true)) {
            blockedMessage = "已拦截危险协议"
            return
        }
        activeTab.url = url
        activeTab.progress = 0
        pendingNavigation = url
    }

    fun onPageStarted(tab: BrowserTab, url: String) {
        tab.url = url
        tab.progress = 5
    }

    fun onPageCommitted(tab: BrowserTab, title: String, url: String, canGoBack: Boolean, canGoForward: Boolean) {
        tab.title = title.ifBlank { url }
        tab.url = url
        tab.progress = 100
        tab.canGoBack = canGoBack
        tab.canGoForward = canGoForward
        library.recordHistory(tab.title, url, tab.isPrivate)
        history = library.history
    }

    fun onHistoryChanged(tab: BrowserTab, url: String, canGoBack: Boolean, canGoForward: Boolean) {
        tab.url = url
        tab.canGoBack = canGoBack
        tab.canGoForward = canGoForward
    }

    fun blockNavigation(reason: String) {
        blockedMessage = reason
    }

    fun toggleBookmark() {
        val tab = activeTab
        if (!UrlNormalizer.isSafeScheme(tab.url)) return
        val existing = bookmarks.firstOrNull { it.url == tab.url }
        if (existing != null) library.removeBookmark(existing.id) else library.addBookmark(Bookmark(title = tab.title, url = tab.url))
        bookmarks = library.bookmarks
    }

    fun isBookmarked(url: String) = bookmarks.any { it.url == url }

    fun removeBookmark(id: String) {
        library.removeBookmark(id)
        bookmarks = library.bookmarks
    }

    fun clearHistory() {
        library.clearHistory()
        history = library.history
    }

    fun enqueueDownload(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long,
    ): String {
        return try {
            val started = Downloads.enqueue(
                getApplication(),
                url,
                userAgent,
                contentDisposition,
                mimeType,
                contentLength,
            )
            downloads.add(
                0,
                DownloadRecord(
                    fileName = started.fileName,
                    url = started.url,
                    totalBytes = started.totalBytes,
                    status = started.status,
                ),
            )
            "已开始下载 ${started.fileName}"
        } catch (_: Exception) {
            val name = url.substringAfterLast('/').substringBefore('?').ifBlank { "下载" }
            downloads.add(0, DownloadRecord(fileName = name, url = url, totalBytes = contentLength, status = "未能开始"))
            "下载未能开始"
        }
    }
}
