package com.tokidiscover.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tokidiscover.app.TokiDiscoverApplication
import com.tokidiscover.app.data.PageActions
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.data.WebPermissionPolicy
import com.tokidiscover.app.ui.browser.HomeScreen
import com.tokidiscover.app.ui.browser.LibraryScreen
import com.tokidiscover.app.ui.browser.SettingsPage
import com.tokidiscover.app.ui.browser.SettingsScreen
import com.tokidiscover.app.ui.browser.TabWebHost
import com.tokidiscover.app.ui.browser.TabsScreen
import com.tokidiscover.app.ui.components.ContentEdge
import com.tokidiscover.app.ui.components.ContentEdgeFades
import com.tokidiscover.app.ui.components.DiscoverOmnibox
import com.tokidiscover.app.ui.components.MoreMenuPanel
import com.tokidiscover.app.ui.components.NavEntry
import com.tokidiscover.app.ui.components.OverlayTitleHeader
import com.tokidiscover.app.ui.components.SideToolbar
import com.tokidiscover.app.ui.components.TopOmniboxPill
import com.tokidiscover.app.ui.theme.ThemeMode
import com.tokidiscover.app.ui.theme.TokiDiscoverTheme
import com.tokidiscover.app.ui.theme.colorFromHex
import com.tokidiscover.app.ui.theme.solidChrome
import com.tokidiscover.app.ui.theme.tokiContentChrome
import com.tokidiscover.app.ui.theme.tokiPageBackground
import com.tokiui.semantic.SemanticTokens
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

private val navEntries = listOf(
    NavEntry(Dest.Browser, Icons.Outlined.Public, Icons.Filled.Public, "浏览"),
    NavEntry(Dest.Tabs, Icons.Outlined.Tab, Icons.Filled.Tab, "标签"),
    NavEntry(Dest.Library, Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark, "书库"),
    NavEntry(Dest.Settings, Icons.Outlined.Settings, Icons.Filled.Settings, "设置"),
)

private val ExpandedWindow = 840.dp

private fun settingsPageTitle(page: SettingsPage): String = when (page) {
    SettingsPage.Main -> "设置"
    SettingsPage.Appearance -> "外观"
    SettingsPage.SearchEngine -> "搜索引擎"
    SettingsPage.Browsing -> "浏览"
    SettingsPage.ClearData -> "清除浏览数据"
    SettingsPage.Update -> "更新"
    SettingsPage.About -> "关于"
}

/** A page's permission request waiting on the user, then on the system prompt. */
private sealed interface PendingWebPermission {
    val origin: String
    val systemPermissions: Array<String>
    fun deny()

    data class Media(val request: PermissionRequest) : PendingWebPermission {
        override val origin: String get() = request.origin.toString()
        override val systemPermissions: Array<String>
            get() = WebPermissionPolicy.systemPermissionsFor(request.resources).toTypedArray()
        override fun deny() {
            runCatching { request.deny() }
        }
    }

    data class Location(
        val site: String,
        val callback: GeolocationPermissions.Callback,
    ) : PendingWebPermission {
        override val origin: String get() = site
        override val systemPermissions: Array<String>
            get() = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        override fun deny() {
            runCatching { callback.invoke(site, false, false) }
        }
    }
}

@Composable
fun TokiDiscoverApp() {
    val context = LocalContext.current
    val app = context.applicationContext as TokiDiscoverApplication
    val vm: BrowserViewModel = viewModel()
    val tokens = remember {
        SemanticTokens.parse(context.assets.open("tokiui.semantic.tokens.json").bufferedReader().use { it.readText() })
    }
    var themeName by rememberSaveable { mutableStateOf(app.settings.themeMode) }
    var useDynamic by rememberSaveable { mutableStateOf(app.settings.useDynamicColor) }
    var seedIndex by rememberSaveable { mutableIntStateOf(app.settings.seedIndex) }
    var reduceMotion by rememberSaveable { mutableStateOf(app.settings.reduceMotion) }
    var backdropBlur by rememberSaveable { mutableStateOf(app.settings.backdropBlur) }
    val mode = runCatching { ThemeMode.valueOf(themeName) }.getOrDefault(ThemeMode.System)
    val seed = colorFromHex(tokens.seeds[seedIndex.coerceIn(tokens.seeds.indices)].value)

    TokiDiscoverTheme(mode, useDynamic, seed, reduceMotion) {
        DiscoverShell(
            vm = vm,
            tokens = tokens,
            mode = mode,
            onMode = { themeName = it.name; app.settings.themeMode = it.name },
            useDynamic = useDynamic,
            onUseDynamic = { useDynamic = it; app.settings.useDynamicColor = it },
            seedIndex = seedIndex,
            onSeed = { seedIndex = it; app.settings.seedIndex = it },
            reduceMotion = reduceMotion,
            onReduceMotion = { reduceMotion = it; app.settings.reduceMotion = it },
            backdropBlur = backdropBlur,
            onBackdropBlur = { backdropBlur = it; app.settings.backdropBlur = it },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DiscoverShell(
    vm: BrowserViewModel,
    tokens: SemanticTokens,
    mode: ThemeMode,
    onMode: (ThemeMode) -> Unit,
    useDynamic: Boolean,
    onUseDynamic: (Boolean) -> Unit,
    seedIndex: Int,
    onSeed: (Int) -> Unit,
    reduceMotion: Boolean,
    onReduceMotion: (Boolean) -> Unit,
    backdropBlur: Boolean,
    onBackdropBlur: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val hazeState = rememberHazeState()
    val glass = tokiContentChrome(hazeState, backdropBlur)
    var destName by rememberSaveable { mutableStateOf(Dest.Browser.name) }
    val dest = runCatching { Dest.valueOf(destName) }.getOrDefault(Dest.Browser)
    fun select(next: Dest) { destName = next.name }
    var settingsPageName by rememberSaveable { mutableStateOf(SettingsPage.Main.name) }
    /** A settings sub-page the ⋮ menu asked for; applied once when the dest changes. */
    var pendingSettingsPageName by remember { mutableStateOf<String?>(null) }
    /** Which Library section the ⋮ menu asked for (0 书签 / 1 历史 / 2 下载). */
    var librarySection by rememberSaveable { mutableIntStateOf(0) }
    val settingsPage = runCatching { SettingsPage.valueOf(settingsPageName) }.getOrDefault(SettingsPage.Main)
    /** Top omnibox pill editing state, hoisted so the bottom domain capsule can open it. Reset per tab. */
    var omniboxEditing by remember(vm.activeTabId) { mutableStateOf(false) }
    var omniboxDraft by remember(vm.activeTabId) { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var findVisible by rememberSaveable { mutableStateOf(false) }
    var findQuery by rememberSaveable { mutableStateOf("") }
    var pendingWebPermission by remember { mutableStateOf<PendingWebPermission?>(null) }
    var resolvingWebPermission by remember { mutableStateOf<PendingWebPermission?>(null) }
    var rendererEpoch by remember { mutableIntStateOf(0) }
    val hostRef = remember { mutableStateOf<TabWebHost?>(null) }
    // The header frost is always on (0.1.10), so pages no longer report a
    // scroll offset; the connection stays only as a nested-scroll hook.
    val nested = remember { object : NestedScrollConnection {} }
    val showingPage = dest == Dest.Browser && vm.activeTab.url != UrlNormalizer.NEW_TAB_URL
    val title = if (dest == Dest.Settings && settingsPage != SettingsPage.Main) {
        settingsPageTitle(settingsPage)
    } else {
        when (dest) {
            Dest.Browser -> if (vm.activeTab.isPrivate) "无痕浏览" else "TokiDiscover"
            Dest.Tabs -> "标签页"
            Dest.Library -> "书库"
            Dest.Settings -> "设置"
        }
    }
    var topBarHeight by remember { mutableStateOf(96.dp) }
    var barHeight by remember { mutableStateOf(0.dp) }
    val insetBottom = with(density) {
        WindowInsets.navigationBars.union(WindowInsets.ime).getBottom(this).toDp()
    }
    val bottomPadding = barHeight + 16.dp
    val surface = MaterialTheme.colorScheme.surface.toArgb()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val webPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val pending = resolvingWebPermission ?: return@rememberLauncherForActivityResult
        resolvingWebPermission = null
        val granted = result.filterValues { it }.keys
        when (pending) {
            is PendingWebPermission.Media -> {
                val grantable = WebPermissionPolicy.grantableResources(pending.request.resources, granted)
                runCatching {
                    if (grantable.isNotEmpty()) pending.request.grant(grantable) else pending.request.deny()
                }
                if (grantable.isEmpty()) {
                    scope.launch { snackbar.showSnackbar("权限被拒绝，可在系统设置中开启") }
                }
            }
            is PendingWebPermission.Location -> {
                val allowed = Manifest.permission.ACCESS_FINE_LOCATION in granted ||
                    Manifest.permission.ACCESS_COARSE_LOCATION in granted
                runCatching { pending.callback.invoke(pending.site, allowed, false) }
                if (!allowed) {
                    scope.launch { snackbar.showSnackbar("权限被拒绝，可在系统设置中开启") }
                }
            }
        }
    }
    val singlePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        hostRef.value?.controller?.deliverFileChooserResult(uri?.let { arrayOf(it) })
    }
    val multiPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        hostRef.value?.controller?.deliverFileChooserResult(uris.takeIf { it.isNotEmpty() }?.toTypedArray())
    }
    fun toast(message: String) { scope.launch { snackbar.showSnackbar(message) } }
    fun startOmniboxEditing() {
        omniboxDraft = if (vm.activeTab.url == UrlNormalizer.NEW_TAB_URL) "" else vm.activeTab.url
        omniboxEditing = true
    }
    fun sharePage() {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, vm.activeTab.url)
        context.startActivity(Intent.createChooser(send, "分享当前页"))
    }

    LaunchedEffect(dest, vm.activeTabId) {
        omniboxEditing = false
        findVisible = false
        menuExpanded = false
        pendingWebPermission?.deny()
        pendingWebPermission = null
        settingsPageName = pendingSettingsPageName ?: SettingsPage.Main.name
        pendingSettingsPageName = null
    }

    val webBack = showingPage && vm.activeTab.canGoBack
    BackHandler(enabled = webBack) { hostRef.value?.controller?.back() }
    BackHandler(enabled = !webBack && dest != Dest.Browser) { select(Dest.Browser) }
    BackHandler(enabled = dest == Dest.Settings && settingsPage != SettingsPage.Main) {
        settingsPageName = SettingsPage.Main.name
    }
    BackHandler(enabled = menuExpanded) { menuExpanded = false }
    BackHandler(enabled = omniboxEditing) { omniboxEditing = false }

    BoxWithConstraints(Modifier.fillMaxSize().background(tokiPageBackground())) {
        val wide = maxWidth >= ExpandedWindow
        Row(Modifier.fillMaxSize()) {
            if (wide) {
                NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxHeight()) {
                    navEntries.forEach { entry ->
                        NavigationRailItem(
                            selected = dest == entry.dest,
                            onClick = { select(entry.dest) },
                            icon = {
                                Icon(
                                    if (dest == entry.dest) entry.selectedIcon else entry.icon,
                                    contentDescription = entry.label,
                                )
                            },
                            label = { Text(entry.label) },
                        )
                    }
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                        .background(tokiPageBackground()),
                ) {
                    AndroidView(
                        factory = { ctx -> TabWebHost(ctx).also { hostRef.value = it } },
                        update = { host ->
                            // Read so a renderer crash recomposes and sync recreates the WebView.
                            if (rendererEpoch < 0) return@AndroidView
                            val controller = host.controller
                            controller.onPageStarted = { tab, url -> vm.onPageStarted(tab, url) }
                            controller.onPageFinished = { tab, pageTitle, url, back, forward ->
                                vm.onPageCommitted(tab, pageTitle, url, back, forward)
                            }
                            controller.onHistoryChanged = { tab, url, back, forward -> vm.onHistoryChanged(tab, url, back, forward) }
                            controller.onBlocked = { vm.blockNavigation(it) }
                            controller.onDownload = { url, agent, disposition, mime, length ->
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                toast(vm.enqueueDownload(url, agent, disposition, mime, length))
                            }
                            controller.onRendererGone = { rendererEpoch++ }
                            controller.onOpenInNewTab = { url, isPrivate -> vm.newTab(isPrivate = isPrivate, url = url) }
                            controller.onFileChooserRequested = { mode, accept ->
                                val mime = accept.firstOrNull { it.isNotBlank() } ?: "*/*"
                                if (mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
                                    multiPicker.launch(mime)
                                } else {
                                    singlePicker.launch(mime)
                                }
                            }
                            controller.onWebPermissionRequest = { request ->
                                if (pendingWebPermission == null && resolvingWebPermission == null) {
                                    pendingWebPermission = PendingWebPermission.Media(request)
                                } else {
                                    runCatching { request.deny() }
                                }
                            }
                            controller.onGeolocationRequest = { site, callback ->
                                if (pendingWebPermission == null && resolvingWebPermission == null) {
                                    pendingWebPermission = PendingWebPermission.Location(site, callback)
                                } else {
                                    runCatching { callback.invoke(site, false, false) }
                                }
                            }
                            host.sync(vm, showingPage, surface)
                        },
                        // The WebView never resizes: no conditional status bar padding.
                        // Resizing on every navigation was repainting the page (flicker).
                        modifier = Modifier.fillMaxSize(),
                    )
                    SideEffect {
                        val pending = vm.pendingNavigation ?: return@SideEffect
                        val host = hostRef.value ?: return@SideEffect
                        host.navigate(vm.activeTab, pending)
                        vm.pendingNavigation = null
                    }
                    // Edge fades over the content: on a web page, top (under
                    // the status bar) and bottom (on the WebView's bottom
                    // edge, above the redundancy gap); on the app pages,
                    // below the frosted header and above the bottom bar.
                    // Plain gradient Boxes — no touch handling, so gestures
                    // reach the WebView / lists untouched.
                    if (showingPage) {
                        ContentEdgeFades(
                            bottomInset = ContentEdge.webBottomInset(),
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        ContentEdgeFades(
                            bottomInset = bottomPadding,
                            topInset = topBarHeight,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    if (!showingPage) {
                        val top = topBarHeight
                        when (dest) {
                            Dest.Browser -> HomeScreen(vm, top, bottomPadding, nested)
                            Dest.Tabs -> TabsScreen(vm, top, bottomPadding, nested) { select(Dest.Browser) }
                            Dest.Library -> LibraryScreen(
                                vm = vm,
                                topPadding = top,
                                bottomPadding = bottomPadding,
                                nestedScroll = nested,
                                onOpenUrl = { url ->
                                    vm.navigate(url)
                                    select(Dest.Browser)
                                },
                                section = librarySection,
                                onSection = { librarySection = it },
                            )
                            Dest.Settings -> SettingsScreen(
                                vm = vm,
                                tokens = tokens,
                                page = settingsPage,
                                onPage = { settingsPageName = it.name },
                                topPadding = top,
                                bottomPadding = bottomPadding,
                                nestedScroll = nested,
                                themeMode = mode,
                                onThemeMode = onMode,
                                useDynamic = useDynamic,
                                onUseDynamic = onUseDynamic,
                                seedIndex = seedIndex,
                                onSeed = onSeed,
                                reduceMotion = reduceMotion,
                                onReduceMotion = onReduceMotion,
                                backdropBlur = backdropBlur,
                                onBackdropBlur = onBackdropBlur,
                                onClearCache = { hostRef.value?.clearCaches() },
                                onMessage = ::toast,
                            )
                        }
                    }
                }
                if (!showingPage) {
                    OverlayTitleHeader(
                        title = title,
                        onBack = if (dest == Dest.Settings && settingsPage != SettingsPage.Main) {
                            { settingsPageName = SettingsPage.Main.name }
                        } else {
                            null
                        },
                        onHeightMeasured = { topBarHeight = it },
                        backdropBlur = backdropBlur,
                        hazeState = hazeState,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
                if (menuExpanded) {
                    // Tapping anywhere outside the menu dismisses it.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { menuExpanded = false },
                    )
                }
                if (showingPage) {
                    // Top floating chrome on a website: the omnibox pill,
                    // then the find row and the page menu dropping down from
                    // it. The ⋮ button lives in the pill; the panel used to
                    // rise from the bottom bar.
                    Column(
                        Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 8.dp, start = 12.dp, end = 12.dp)
                            .widthIn(max = 840.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TopOmniboxPill(
                            tab = vm.activeTab,
                            editing = omniboxEditing,
                            draft = omniboxDraft,
                            onDraftChange = { omniboxDraft = it },
                            onEditRequest = { startOmniboxEditing() },
                            onSubmit = { vm.submitOmnibox(it); omniboxEditing = false },
                            onEditingChange = { omniboxEditing = it },
                            onMenuClick = { menuExpanded = !menuExpanded },
                            glass = glass,
                            solidStroke = solidChrome(backdropBlur),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (findVisible) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.extraLarge)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TextField(
                                    value = findQuery,
                                    onValueChange = {
                                        findQuery = it
                                        hostRef.value?.controller?.find(it)
                                    },
                                    label = { Text("在页面中查找") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { hostRef.value?.controller?.findNext() }),
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = { hostRef.value?.controller?.findNext() }) { Text("下一个") }
                                TextButton(onClick = {
                                    findVisible = false
                                    hostRef.value?.controller?.clearMatches()
                                }) { Text("关闭") }
                            }
                        }
                        AnimatedVisibility(
                            visible = menuExpanded,
                            enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec(), expandFrom = Alignment.Top) +
                                fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                            exit = shrinkVertically(MaterialTheme.motionScheme.defaultSpatialSpec(), shrinkTowards = Alignment.Top) +
                                fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec()),
                        ) {
                            MoreMenuPanel(
                                bookmarked = vm.isBookmarked(vm.activeTab.url),
                                onBookmark = { menuExpanded = false; vm.toggleBookmark() },
                                onShare = { menuExpanded = false; sharePage() },
                                onFind = { menuExpanded = false; findVisible = true },
                                onTranslate = {
                                    menuExpanded = false
                                    if (UrlNormalizer.isSafeScheme(vm.activeTab.url)) {
                                        vm.navigate(PageActions.translateUrl(vm.activeTab.url))
                                    }
                                },
                                onNewTab = { menuExpanded = false; vm.newTab(); select(Dest.Browser) },
                                onNewPrivateTab = { menuExpanded = false; vm.newTab(isPrivate = true); select(Dest.Browser) },
                                onAddToGroup = {
                                    menuExpanded = false
                                    val tab = vm.activeTab
                                    val existing = vm.groups.firstOrNull { tab.id in it.tabIds }
                                    if (existing != null) {
                                        toast("该标签页已在群组「${existing.name}」中")
                                    } else {
                                        val group = vm.createGroupForTab(tab.id)
                                        if (group != null) toast("已创建群组「${group.name}」") else toast("无法创建群组")
                                    }
                                },
                                onHistory = { menuExpanded = false; librarySection = 1; select(Dest.Library) },
                                onClearData = {
                                    menuExpanded = false
                                    pendingSettingsPageName = SettingsPage.ClearData.name
                                    select(Dest.Settings)
                                },
                                onDownloads = { menuExpanded = false; librarySection = 2; select(Dest.Library) },
                                onBookmarks = { menuExpanded = false; librarySection = 0; select(Dest.Library) },
                                onRecentTabs = { menuExpanded = false; select(Dest.Tabs) },
                                onTabs = { menuExpanded = false; select(Dest.Tabs) },
                                onSettings = {
                                    menuExpanded = false
                                    pendingSettingsPageName = SettingsPage.Main.name
                                    select(Dest.Settings)
                                },
                                onCustomizeNewTab = {
                                    menuExpanded = false
                                    pendingSettingsPageName = SettingsPage.Browsing.name
                                    select(Dest.Settings)
                                },
                                onAbout = {
                                    menuExpanded = false
                                    pendingSettingsPageName = SettingsPage.About.name
                                    select(Dest.Settings)
                                },
                                glass = glass,
                                solidStroke = solidChrome(backdropBlur),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    // Right-side vertical toolbar: the page actions that used
                    // to live in the bottom function bar.
                    SideToolbar(
                        bookmarked = vm.isBookmarked(vm.activeTab.url),
                        loading = vm.activeTab.progress in 1..99,
                        canGoBack = vm.activeTab.canGoBack,
                        canGoForward = vm.activeTab.canGoForward,
                        onBack = { hostRef.value?.controller?.back() },
                        onForward = { hostRef.value?.controller?.forward() },
                        onHome = { vm.navigate(UrlNormalizer.NEW_TAB_URL) },
                        onFind = { menuExpanded = false; findVisible = true },
                        onTabs = { select(Dest.Tabs) },
                        onBookmark = { vm.toggleBookmark() },
                        onHistory = { librarySection = 1; select(Dest.Library) },
                        onReloadOrStop = { hostRef.value?.controller?.reloadOrStop(vm.activeTab.progress in 1..99) },
                        onShare = { sharePage() },
                        onSettings = {
                            pendingSettingsPageName = SettingsPage.Main.name
                            select(Dest.Settings)
                        },
                        glass = glass,
                        solidStroke = solidChrome(backdropBlur),
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
                    )
                }
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { barHeight = with(density) { it.height.toDp() } }
                        .padding(bottom = insetBottom + 8.dp, start = 12.dp, end = 12.dp)
                        .widthIn(max = 840.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (!wide || showingPage) {
                        // Bottom bar only: the find row and the page menu
                        // moved up to the top omnibox pill.
                        DiscoverOmnibox(
                            tab = vm.activeTab,
                            showingPage = showingPage,
                            reduceMotion = reduceMotion,
                            glass = glass,
                            solidStroke = solidChrome(backdropBlur),
                            entries = navEntries,
                            current = dest,
                            onSelectDest = { select(it) },
                            onExpand = { menuExpanded = false; startOmniboxEditing() },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                SnackbarHost(
                    snackbar,
                    Modifier.align(Alignment.BottomCenter).padding(bottom = bottomPadding),
                )
            }
        }
    }
    vm.blockedMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { vm.blockedMessage = null },
            confirmButton = { TextButton(onClick = { vm.blockedMessage = null }) { Text("知道了") } },
            title = { Text("已拦截") },
            text = { Text(message) },
        )
    }
    pendingWebPermission?.let { pending ->
        val host = WebPermissionPolicy.originHost(pending.origin)
        val labels = when (pending) {
            is PendingWebPermission.Media -> WebPermissionPolicy.resourceLabels(pending.request.resources)
            is PendingWebPermission.Location -> listOf("位置信息")
        }
        AlertDialog(
            onDismissRequest = {
                pendingWebPermission = null
                pending.deny()
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingWebPermission = null
                    resolvingWebPermission = pending
                    webPermissionLauncher.launch(pending.systemPermissions)
                }) { Text("允许") }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingWebPermission = null
                    pending.deny()
                }) { Text("不允许") }
            },
            title = { Text("网站权限请求") },
            text = { Text("「$host」想要使用${labels.joinToString("、")}。允许后，系统会弹出权限请求。") },
        )
    }
}
