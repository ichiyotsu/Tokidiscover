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
import com.tokidiscover.app.data.ClosedTab
import com.tokidiscover.app.data.DownloadRecord
import com.tokidiscover.app.data.Downloads
import com.tokidiscover.app.data.FunctionBarComponent
import com.tokidiscover.app.data.NavResult
import com.tokidiscover.app.data.SpeedDialSite
import com.tokidiscover.app.data.TabGroup
import com.tokidiscover.app.data.TabSnapshot
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.data.addTabToGroup
import com.tokidiscover.app.data.closeTabGroup
import com.tokidiscover.app.data.createTabGroup
import com.tokidiscover.app.data.decodeFunctionBarItems
import com.tokidiscover.app.data.decodeSpeedDial
import com.tokidiscover.app.data.decodeTabGroups
import com.tokidiscover.app.data.deleteTabGroup
import com.tokidiscover.app.data.encodeFunctionBarItems
import com.tokidiscover.app.data.encodeSpeedDial
import com.tokidiscover.app.data.encodeTabGroups
import com.tokidiscover.app.data.groupOfTab
import com.tokidiscover.app.data.moveFunctionBarItem
import com.tokidiscover.app.data.recordClosedTab
import com.tokidiscover.app.data.removeTabFromGroup
import com.tokidiscover.app.data.renameTabGroup
import com.tokidiscover.app.data.sanitizeFunctionBarItems

/** Owns the in-memory tab list and library. One WebView per tab is kept by the browser host until the tab closes. */
class BrowserViewModel(app: Application) : AndroidViewModel(app) {
    val settings get() = getApplication<TokiDiscoverApplication>().settings
    val library get() = getApplication<TokiDiscoverApplication>().library
    val tabs = mutableStateListOf<BrowserTab>()
    var activeTabId by mutableStateOf("")
        private set
    /** Tab groups, restored from storage on launch (their tabs are restored with them). */
    var groups by mutableStateOf<List<TabGroup>>(emptyList())
        private set
    /** Tabs closed this session, newest first; private tabs and blank new tabs are not kept. */
    val recentlyClosed = mutableStateListOf<ClosedTab>()
    /** Which Library section the menu asked for (0 书签 / 1 历史 / 2 下载). */
    var librarySection by mutableStateOf(0)

    init {
        val (savedGroups, snapshots) = decodeTabGroups(library.tabGroupsJson)
        val restored = snapshots.map {
            BrowserTab(id = it.tabId, url = it.url, title = it.title, isPrivate = it.isPrivate)
        }
        if (restored.isNotEmpty()) {
            tabs.addAll(restored)
            activeTabId = restored.first().id
            groups = savedGroups
        } else {
            val tab = BrowserTab()
            tabs.add(tab)
            activeTabId = tab.id
        }
    }

    private fun saveGroups() {
        library.tabGroupsJson = encodeTabGroups(
            groups,
            tabs.map { TabSnapshot(it.id, it.title, it.url, it.isPrivate) },
        )
    }

    fun createGroupForTab(tabId: String): TabGroup? {
        if (tabs.none { it.id == tabId }) return null
        val (updated, group) = createTabGroup(groups, "", listOf(tabId))
        groups = updated
        saveGroups()
        return group
    }

    fun addTabToGroup(tabId: String, groupId: String) {
        groups = addTabToGroup(groups, groupId, tabId)
        saveGroups()
    }

    fun ungroupTab(tabId: String) {
        val group = groupOfTab(groups, tabId) ?: return
        groups = removeTabFromGroup(groups, group.id, tabId)
        saveGroups()
    }

    fun renameGroup(groupId: String, name: String) {
        groups = renameTabGroup(groups, groupId, name)
        saveGroups()
    }

    fun deleteGroup(groupId: String) {
        groups = deleteTabGroup(groups, groupId)
        saveGroups()
    }

    fun closeGroup(groupId: String) {
        val (remaining, tabIds) = closeTabGroup(groups, groupId)
        groups = remaining
        tabIds.forEach { closeTab(it) }
        saveGroups()
    }

    fun closeAllTabs() {
        tabs.toList().forEach { closeTab(it.id) }
        groups = emptyList()
        saveGroups()
    }

    fun restoreClosedTab(closed: ClosedTab) {
        recentlyClosed.remove(closed)
        newTab(isPrivate = closed.isPrivate, url = closed.url)
    }
    val activeTab: BrowserTab get() = tabs.firstOrNull { it.id == activeTabId } ?: tabs.first()
    var bookmarks by mutableStateOf(library.bookmarks)
        private set
    var history by mutableStateOf(library.history)
        private set
    /** Speed Dial sites, restored from storage (defaults when nothing custom is saved). */
    var speedDial by mutableStateOf(decodeSpeedDial(library.speedDialJson))
        private set
    /** Bottom function bar layout, restored from storage. */
    var functionBarItems by mutableStateOf(decodeFunctionBarItems(settings.functionBarJson))
        private set

    /**
     * Add a Speed Dial site. The input is normalized like an omnibox entry
     * (a bare host becomes https://…, a search phrase becomes the search
     * URL); blank title falls back to the host. Returns false when the
     * input is not navigable or the URL is already on the dial.
     */
    fun addSpeedDial(title: String, input: String): Boolean {
        val url = when (val result = UrlNormalizer.normalize(input.trim(), settings.engine(), settings.customTemplate)) {
            is NavResult.Url -> result.url
            is NavResult.Search -> result.url
            NavResult.NewTab -> return false
            is NavResult.Blocked -> return false
        }
        if (speedDial.any { it.url == url }) return false
        val name = title.trim().ifBlank { url.substringAfter("://", url).substringBefore('/').ifBlank { url } }
        speedDial = speedDial + SpeedDialSite(name, url)
        library.speedDialJson = encodeSpeedDial(speedDial)
        return true
    }

    fun removeSpeedDial(site: SpeedDialSite) {
        speedDial = speedDial.filterNot { it.url == site.url }
        library.speedDialJson = encodeSpeedDial(speedDial)
    }

    private fun saveFunctionBar() {
        settings.functionBarJson = encodeFunctionBarItems(functionBarItems)
    }

    fun updateFunctionBarItems(items: List<FunctionBarComponent>) {
        functionBarItems = sanitizeFunctionBarItems(items)
        saveFunctionBar()
    }

    fun moveFunctionBarComponent(from: Int, to: Int) {
        val moved = moveFunctionBarItem(functionBarItems, from, to)
        if (moved != functionBarItems) {
            functionBarItems = moved
            saveFunctionBar()
        }
    }

    /** Show/hide one bar component. Settings can never be hidden. */
    fun setFunctionBarComponentVisible(component: FunctionBarComponent, visible: Boolean) {
        if (component == FunctionBarComponent.Settings) return
        val updated = if (visible) {
            if (component in functionBarItems) functionBarItems else functionBarItems + component
        } else {
            functionBarItems - component
        }
        updateFunctionBarItems(updated)
    }

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
        val tab = tabs[index]
        if (!tab.isPrivate && tab.url != UrlNormalizer.NEW_TAB_URL) {
            val kept = recordClosedTab(
                recentlyClosed.toList(),
                ClosedTab(tab.title.ifBlank { tab.url }, tab.url, tab.isPrivate),
            )
            recentlyClosed.clear()
            recentlyClosed.addAll(kept)
        }
        groups = groups.map { it.copy(tabIds = it.tabIds - id) }.filter { it.tabIds.isNotEmpty() }
        tabs.removeAt(index)
        if (tabs.isEmpty()) {
            val fresh = BrowserTab()
            tabs.add(fresh)
            activeTabId = fresh.id
        } else if (activeTabId == id) {
            activeTabId = tabs[minOf(index, tabs.lastIndex)].id
        }
        saveGroups()
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
