package com.tokidiscover.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tokidiscover.app.TokiDiscoverApplication
import com.tokidiscover.app.data.PageActions
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.ui.browser.HomeScreen
import com.tokidiscover.app.ui.browser.LibraryScreen
import com.tokidiscover.app.ui.browser.SettingsScreen
import com.tokidiscover.app.ui.browser.TabWebHost
import com.tokidiscover.app.ui.browser.TabsScreen
import com.tokidiscover.app.ui.components.DiscoverNavBar
import com.tokidiscover.app.ui.components.DiscoverOmnibox
import com.tokidiscover.app.ui.components.NavEntry
import com.tokidiscover.app.ui.components.OverlayTitle
import com.tokidiscover.app.ui.theme.ThemeMode
import com.tokidiscover.app.ui.theme.TokiDiscoverTheme
import com.tokidiscover.app.ui.theme.colorFromHex
import com.tokidiscover.app.ui.theme.solidChrome
import com.tokidiscover.app.ui.theme.tokiChrome
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
    val glass = tokiChrome(hazeState, backdropBlur)
    var destName by rememberSaveable { mutableStateOf(Dest.Browser.name) }
    val dest = runCatching { Dest.valueOf(destName) }.getOrDefault(Dest.Browser)
    fun select(next: Dest) { destName = next.name }
    var collapsed by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var findVisible by rememberSaveable { mutableStateOf(false) }
    var findQuery by rememberSaveable { mutableStateOf("") }
    var rendererEpoch by remember { mutableIntStateOf(0) }
    val hostRef = remember { mutableStateOf<TabWebHost?>(null) }
    val headerOffset = remember { mutableFloatStateOf(0f) }
    val nested = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                headerOffset.floatValue = headerScrollOffset(headerOffset.floatValue, consumed.y, available.y)
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y > 0f) headerOffset.floatValue = 0f
                return Velocity.Zero
            }
        }
    }
    val overlapped = headerOverlapsContent(headerOffset.floatValue, with(density) { 8.dp.toPx() })
    val showingPage = dest == Dest.Browser && vm.activeTab.url != UrlNormalizer.NEW_TAB_URL
    val title = when (dest) {
        Dest.Browser -> if (vm.activeTab.isPrivate) "无痕浏览" else "TokiDiscover"
        Dest.Tabs -> "标签页"
        Dest.Library -> "书库"
        Dest.Settings -> "设置"
    }
    var topBarHeight by remember { mutableStateOf(96.dp) }
    var barHeight by remember { mutableStateOf(0.dp) }
    val insetBottom = with(density) {
        WindowInsets.navigationBars.union(WindowInsets.ime).getBottom(this).toDp()
    }
    val bottomPadding = barHeight + 16.dp
    val surface = MaterialTheme.colorScheme.surface.toArgb()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun toast(message: String) { scope.launch { snackbar.showSnackbar(message) } }

    LaunchedEffect(dest, vm.activeTabId) {
        headerOffset.floatValue = 0f
        collapsed = false
        editing = false
        findVisible = false
    }

    val webBack = showingPage && vm.activeTab.canGoBack
    BackHandler(enabled = webBack) { hostRef.value?.controller?.back() }
    BackHandler(enabled = !webBack && dest != Dest.Browser) { select(Dest.Browser) }

    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
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
                        .background(MaterialTheme.colorScheme.surface),
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
                            controller.onScroll = { dy -> collapsed = nextOmniboxCollapsed(collapsed, dy, editing) }
                            controller.onRendererGone = { rendererEpoch++ }
                            host.sync(vm, showingPage, surface)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (showingPage) Modifier.statusBarsPadding() else Modifier),
                    )
                    SideEffect {
                        val pending = vm.pendingNavigation ?: return@SideEffect
                        val host = hostRef.value ?: return@SideEffect
                        host.navigate(vm.activeTab, pending)
                        vm.pendingNavigation = null
                    }
                    if (!showingPage) {
                        val top = topBarHeight
                        when (dest) {
                            Dest.Browser -> HomeScreen(vm, top, bottomPadding, nested)
                            Dest.Tabs -> TabsScreen(vm, top, bottomPadding, nested) { select(Dest.Browser) }
                            Dest.Library -> LibraryScreen(vm, top, bottomPadding, nested) { url ->
                                vm.navigate(url)
                                select(Dest.Browser)
                            }
                            Dest.Settings -> SettingsScreen(
                                vm = vm,
                                tokens = tokens,
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
                    OverlayTitle(
                        title = title,
                        overlapped = overlapped,
                        backdropBlur = backdropBlur,
                        hazeState = hazeState,
                        onHeight = { topBarHeight = it },
                        modifier = Modifier.align(Alignment.TopCenter),
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
                    if (findVisible && showingPage) {
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
                    if (showingPage) {
                        DiscoverOmnibox(
                            tab = vm.activeTab,
                            collapsed = collapsed,
                            tabCount = vm.tabs.size,
                            bookmarked = vm.isBookmarked(vm.activeTab.url),
                            glass = glass,
                            onSubmit = { vm.submitOmnibox(it) },
                            onBack = { hostRef.value?.controller?.back() },
                            onForward = { hostRef.value?.controller?.forward() },
                            onReloadOrStop = { hostRef.value?.controller?.reloadOrStop(vm.activeTab.progress in 1..99) },
                            onTabs = { select(Dest.Tabs) },
                            onBookmark = { vm.toggleBookmark() },
                            onShare = {
                                val send = Intent(Intent.ACTION_SEND)
                                    .setType("text/plain")
                                    .putExtra(Intent.EXTRA_TEXT, vm.activeTab.url)
                                context.startActivity(Intent.createChooser(send, "分享当前页"))
                            },
                            onFind = { findVisible = true },
                            onTranslate = {
                                if (UrlNormalizer.isSafeScheme(vm.activeTab.url)) vm.navigate(PageActions.translateUrl(vm.activeTab.url))
                            },
                            onEditing = { editing = it },
                            onExpand = { collapsed = false },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (!wide) {
                        DiscoverNavBar(navEntries, dest, ::select, glass, solidStroke = solidChrome(backdropBlur))
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
}
