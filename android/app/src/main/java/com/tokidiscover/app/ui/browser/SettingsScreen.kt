package com.tokidiscover.app.ui.browser

import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tokidiscover.app.BuildConfig
import com.tokidiscover.app.data.SearchEngine
import com.tokidiscover.app.data.UpdateChecker
import com.tokidiscover.app.ui.BrowserViewModel
import com.tokidiscover.app.ui.theme.ThemeMode
import com.tokidiscover.app.ui.theme.colorFromHex
import com.tokiui.semantic.SemanticTokens
import kotlinx.coroutines.launch

/** 设置内的页面：主页（入口列表）+ 二级页。页面状态由 App 骨架持有，负责返回键。 */
enum class SettingsPage { Main, Appearance, SearchEngine, Browsing, ClearData, Update, About }

private fun SemanticTokens.spaceDp(key: String, fallback: Int): Dp = (space[key] ?: fallback).dp
private fun SemanticTokens.shapeDp(key: String, fallback: Int): Dp = (shape[key] ?: fallback).dp

@Composable
fun SettingsScreen(
    vm: BrowserViewModel,
    tokens: SemanticTokens,
    page: SettingsPage,
    onPage: (SettingsPage) -> Unit,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    useDynamic: Boolean,
    onUseDynamic: (Boolean) -> Unit,
    seedIndex: Int,
    onSeed: (Int) -> Unit,
    reduceMotion: Boolean,
    onReduceMotion: (Boolean) -> Unit,
    backdropBlur: Boolean,
    onBackdropBlur: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val margin = tokens.spaceDp("lg", 16)
    val cardShape = RoundedCornerShape(tokens.shapeDp("large", 28))
    val gap = tokens.spaceDp("md", 12)
    when (page) {
        SettingsPage.Main -> SettingsMainPage(
            vm = vm,
            onPage = onPage,
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            nestedScroll = nestedScroll,
            margin = margin,
            gap = gap,
            cardShape = cardShape,
            tokens = tokens,
        )
        SettingsPage.Appearance -> AppearancePage(
            tokens = tokens,
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            nestedScroll = nestedScroll,
            margin = margin,
            gap = gap,
            cardShape = cardShape,
            themeMode = themeMode,
            onThemeMode = onThemeMode,
            useDynamic = useDynamic,
            onUseDynamic = onUseDynamic,
            seedIndex = seedIndex,
            onSeed = onSeed,
            reduceMotion = reduceMotion,
            onReduceMotion = onReduceMotion,
            backdropBlur = backdropBlur,
            onBackdropBlur = onBackdropBlur,
        )
        SettingsPage.SearchEngine -> SearchEnginePage(
            vm = vm,
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            nestedScroll = nestedScroll,
            margin = margin,
            gap = gap,
            cardShape = cardShape,
            tokens = tokens,
        )
        SettingsPage.Browsing -> BrowsingPage(
            vm = vm,
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            nestedScroll = nestedScroll,
            margin = margin,
            gap = gap,
            cardShape = cardShape,
            tokens = tokens,
        )
        SettingsPage.ClearData -> ClearDataPage(
            vm = vm,
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            nestedScroll = nestedScroll,
            margin = margin,
            gap = gap,
            cardShape = cardShape,
            tokens = tokens,
            onClearCache = onClearCache,
            onMessage = onMessage,
        )
        SettingsPage.Update -> UpdatePage(
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            nestedScroll = nestedScroll,
            margin = margin,
            gap = gap,
            cardShape = cardShape,
            tokens = tokens,
        )
        SettingsPage.About -> AboutPage(
            tokens = tokens,
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            nestedScroll = nestedScroll,
            margin = margin,
            gap = gap,
            cardShape = cardShape,
        )
    }
}

/** 设置分组：surface 阶梯上的实色容器。 */
@Composable
private fun SettingsGroup(cardShape: Shape, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) { content() }
}

@Composable
private fun GroupHeader(tokens: SemanticTokens, icon: ImageVector, title: String) {
    Row(
        Modifier.padding(
            start = tokens.spaceDp("lg", 16),
            top = tokens.spaceDp("lg", 16),
            end = tokens.spaceDp("lg", 16),
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(tokens.spaceDp("md", 12)))
        Text(title, style = MaterialTheme.typography.titleSmall)
    }
}

/** 一行设置：图标 + 标题 + 说明 + 右侧控件。 */
@Composable
private fun SettingRow(
    tokens: SemanticTokens,
    icon: ImageVector,
    title: String,
    note: String? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(60.dp).padding(horizontal = tokens.spaceDp("lg", 16)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(tokens.spaceDp("md", 12)))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (note != null) {
                Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing()
    }
}

/** 设置主页的入口行：图标 + 标题 + 说明 + 右箭头，整行可点进二级页。 */
@Composable
private fun NavRow(tokens: SemanticTokens, icon: ImageVector, title: String, note: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(64.dp).clickable { onClick() }.padding(horizontal = tokens.spaceDp("lg", 16)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(tokens.spaceDp("md", 12)))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SettingsMainPage(
    vm: BrowserViewModel,
    onPage: (SettingsPage) -> Unit,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
    margin: Dp,
    gap: Dp,
    cardShape: Shape,
    tokens: SemanticTokens,
) {
    LazyColumn(
        Modifier.fillMaxSize().nestedScroll(nestedScroll),
        contentPadding = PaddingValues(start = margin, end = margin, top = topPadding + gap, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        item {
            SettingsGroup(cardShape) {
                Column {
                    NavRow(tokens, Icons.Filled.Palette, "外观", "深浅色 · 动态取色 · 种子色 · 动效 · 玻璃模糊") {
                        onPage(SettingsPage.Appearance)
                    }
                    NavRow(tokens, Icons.Filled.Search, "搜索引擎", "当前：${vm.settings.engine().displayName}") {
                        onPage(SettingsPage.SearchEngine)
                    }
                    NavRow(tokens, Icons.Filled.Public, "浏览", "桌面版 UA · JavaScript") {
                        onPage(SettingsPage.Browsing)
                    }
                }
            }
        }
        item {
            SettingsGroup(cardShape) {
                Column {
                    NavRow(tokens, Icons.Filled.DeleteSweep, "清除浏览数据", "Cookie · 缓存 · 历史") {
                        onPage(SettingsPage.ClearData)
                    }
                    NavRow(tokens, Icons.Filled.SystemUpdate, "更新", "当前版本 ${BuildConfig.VERSION_NAME} · 检查更新") {
                        onPage(SettingsPage.Update)
                    }
                    NavRow(tokens, Icons.Filled.Info, "关于", "版本与引擎信息") {
                        onPage(SettingsPage.About)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearancePage(
    tokens: SemanticTokens,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
    margin: Dp,
    gap: Dp,
    cardShape: Shape,
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    useDynamic: Boolean,
    onUseDynamic: (Boolean) -> Unit,
    seedIndex: Int,
    onSeed: (Int) -> Unit,
    reduceMotion: Boolean,
    onReduceMotion: (Boolean) -> Unit,
    backdropBlur: Boolean,
    onBackdropBlur: (Boolean) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    LazyColumn(
        Modifier.fillMaxSize().nestedScroll(nestedScroll),
        contentPadding = PaddingValues(start = margin, end = margin, top = topPadding + gap, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        item {
            SettingsGroup(cardShape) {
                Column {
                    GroupHeader(tokens, Icons.Filled.DarkMode, "深浅色")
                    FlowRow(
                        Modifier.fillMaxWidth().padding(tokens.spaceDp("lg", 16)),
                        horizontalArrangement = Arrangement.spacedBy(tokens.spaceDp("sm", 8)),
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { onThemeMode(mode) },
                                label = { Text(themeLabel(mode)) },
                            )
                        }
                    }
                }
            }
        }
        item {
            SettingsGroup(cardShape) {
                Column {
                    SettingRow(
                        tokens,
                        Icons.Filled.Wallpaper,
                        "动态取色",
                        note = if (useDynamic) "使用系统壁纸的颜色" else "使用下面选中的 TokiUI 种子色",
                        trailing = { Switch(checked = useDynamic, onCheckedChange = onUseDynamic) },
                    )
                    GroupHeader(tokens, Icons.Filled.Palette, "种子色")
                    Row(
                        Modifier.fillMaxWidth().padding(tokens.spaceDp("lg", 16)),
                        horizontalArrangement = Arrangement.spacedBy(tokens.spaceDp("md", 12)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        tokens.seeds.forEachIndexed { index, seed ->
                            val selected = !useDynamic && seedIndex == index
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier.size(40.dp).clip(CircleShape)
                                        .background(colorFromHex(seed.value))
                                        .then(
                                            if (selected) Modifier.border(3.dp, scheme.primary, CircleShape) else Modifier,
                                        )
                                        .clickable { onUseDynamic(false); onSeed(index) },
                                )
                                Text(seed.name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (useDynamic) {
                            Text(
                                "当前使用动态取色，点选种子色即切换为种子方案",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
        item {
            SettingsGroup(cardShape) {
                Column {
                    SettingRow(
                        tokens,
                        Icons.Filled.MotionPhotosOff,
                        "减少动效",
                        note = if (reduceMotion) "已改用标准动效" else "使用表现力动效",
                        trailing = { Switch(checked = reduceMotion, onCheckedChange = onReduceMotion) },
                    )
                    SettingRow(
                        tokens,
                        Icons.Filled.BlurOn,
                        "玻璃模糊",
                        note = if (backdropBlur) "胶囊里采样窗口内容" else "胶囊改为不透明表面",
                        trailing = { Switch(checked = backdropBlur, onCheckedChange = onBackdropBlur) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchEnginePage(
    vm: BrowserViewModel,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
    margin: Dp,
    gap: Dp,
    cardShape: Shape,
    tokens: SemanticTokens,
) {
    var engine by remember { mutableStateOf(vm.settings.engine()) }
    var template by remember { mutableStateOf(vm.settings.customTemplate) }
    val templateMissingQuery = engine == SearchEngine.Custom && !template.contains("{query}")
    LazyColumn(
        Modifier.fillMaxSize().nestedScroll(nestedScroll),
        contentPadding = PaddingValues(start = margin, end = margin, top = topPadding + gap, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        item {
            SettingsGroup(cardShape) {
                Column {
                    GroupHeader(tokens, Icons.Filled.Search, "搜索引擎")
                    Column(Modifier.padding(tokens.spaceDp("lg", 16)), verticalArrangement = Arrangement.spacedBy(gap)) {
                        Text(
                            "当前：${engine.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(tokens.spaceDp("sm", 8)),
                            verticalArrangement = Arrangement.spacedBy(tokens.spaceDp("sm", 8)),
                        ) {
                            SearchEngine.entries.forEach { choice ->
                                FilterChip(
                                    selected = engine == choice,
                                    onClick = {
                                        engine = choice
                                        vm.settings.searchEngine = choice.name
                                    },
                                    label = { Text(choice.displayName) },
                                )
                            }
                        }
                        if (engine == SearchEngine.Custom) {
                            TextField(
                                value = template,
                                onValueChange = {
                                    template = it
                                    vm.settings.customTemplate = it
                                },
                                label = { Text("自定义模板") },
                                supportingText = {
                                    Text(if (templateMissingQuery) "模板里要有 {query}。没有的话，查询会直接接在模板后面。" else "例如 https://example.com/search?q={query}")
                                },
                                isError = templateMissingQuery,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowsingPage(
    vm: BrowserViewModel,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
    margin: Dp,
    gap: Dp,
    cardShape: Shape,
    tokens: SemanticTokens,
) {
    var desktopUa by remember { mutableStateOf(vm.settings.desktopUa) }
    var javascript by remember { mutableStateOf(vm.settings.javaScriptEnabled) }
    LazyColumn(
        Modifier.fillMaxSize().nestedScroll(nestedScroll),
        contentPadding = PaddingValues(start = margin, end = margin, top = topPadding + gap, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        item {
            SettingsGroup(cardShape) {
                Column {
                    SettingRow(
                        tokens,
                        Icons.Filled.DesktopWindows,
                        "桌面版 UA",
                        note = if (desktopUa) "正在使用桌面浏览器标识，刷新后生效" else "使用系统 WebView 标识，刷新后生效",
                        trailing = {
                            Switch(checked = desktopUa, onCheckedChange = { desktopUa = it; vm.settings.desktopUa = it })
                        },
                    )
                    SettingRow(
                        tokens,
                        Icons.Filled.Code,
                        "JavaScript",
                        note = if (javascript) "脚本已打开，刷新后生效" else "脚本已关闭，部分网站无法使用，刷新后生效",
                        trailing = {
                            Switch(checked = javascript, onCheckedChange = { javascript = it; vm.settings.javaScriptEnabled = it })
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ClearDataPage(
    vm: BrowserViewModel,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
    margin: Dp,
    gap: Dp,
    cardShape: Shape,
    tokens: SemanticTokens,
    onClearCache: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    var pendingClear by remember { mutableStateOf<String?>(null) }
    LazyColumn(
        Modifier.fillMaxSize().nestedScroll(nestedScroll),
        contentPadding = PaddingValues(start = margin, end = margin, top = topPadding + gap, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        item {
            SettingsGroup(cardShape) {
                Column {
                    SettingRow(tokens, Icons.Filled.Cookie, "Cookie", note = "登录状态会被清除", trailing = {
                        TextButton(onClick = { pendingClear = "Cookie" }) { Text("清除") }
                    })
                    SettingRow(tokens, Icons.Filled.Cached, "缓存", note = "页面缓存与本地存储", trailing = {
                        TextButton(onClick = { pendingClear = "缓存" }) { Text("清除") }
                    })
                    SettingRow(tokens, Icons.Filled.History, "历史", note = "书库里的浏览历史", trailing = {
                        TextButton(onClick = { pendingClear = "历史" }) { Text("清除") }
                    })
                }
            }
        }
    }
    val clear = pendingClear
    if (clear != null) {
        AlertDialog(
            onDismissRequest = { pendingClear = null },
            title = { Text("清除$clear") },
            text = { Text("取消会保留现有数据。") },
            confirmButton = {
                TextButton(onClick = {
                    when (clear) {
                        "Cookie" -> {
                            CookieManager.getInstance().removeAllCookies(null)
                            CookieManager.getInstance().flush()
                            onMessage("已清除 Cookie")
                        }
                        "缓存" -> {
                            WebStorage.getInstance().deleteAllData()
                            runCatching {
                                WebView(context).apply {
                                    clearCache(true)
                                    destroy()
                                }
                            }
                            onClearCache()
                            onMessage("已清除缓存")
                        }
                        "历史" -> {
                            vm.clearHistory()
                            onMessage("已清除历史")
                        }
                    }
                    pendingClear = null
                }) { Text("清除") }
            },
            dismissButton = { TextButton(onClick = { pendingClear = null }) { Text("取消") } },
        )
    }
}

@Composable
private fun UpdatePage(
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
    margin: Dp,
    gap: Dp,
    cardShape: Shape,
    tokens: SemanticTokens,
) {
    val scope = rememberCoroutineScope()
    var updateText by remember { mutableStateOf("尚未检查") }
    var checking by remember { mutableStateOf(false) }
    LazyColumn(
        Modifier.fillMaxSize().nestedScroll(nestedScroll),
        contentPadding = PaddingValues(start = margin, end = margin, top = topPadding + gap, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        item {
            SettingsGroup(cardShape) {
                Column(
                    Modifier.padding(tokens.spaceDp("lg", 16)),
                    verticalArrangement = Arrangement.spacedBy(tokens.spaceDp("md", 12)),
                ) {
                    Text("当前版本", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("TokiDiscover ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "更新来自 TokiDiscover 仓库的 GitHub Releases。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (checking) LinearProgressIndicator(Modifier.fillMaxWidth())
                    Button(
                        enabled = !checking,
                        onClick = {
                            checking = true
                            updateText = "正在检查…"
                            scope.launch {
                                val latest = UpdateChecker.fetchLatestVersion()
                                updateText = when {
                                    latest == null -> "暂时无法检查。可能没有网络，或仓库还没有正式版本。"
                                    UpdateChecker.isNewer(latest, BuildConfig.VERSION_NAME) -> "发现新版本 $latest"
                                    else -> "已是最新版本"
                                }
                                checking = false
                            }
                        },
                    ) { Text(if (checking) "检查中…" else "检查更新") }
                    Text(updateText, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun AboutPage(
    tokens: SemanticTokens,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
    margin: Dp,
    gap: Dp,
    cardShape: Shape,
) {
    val context = LocalContext.current
    LazyColumn(
        Modifier.fillMaxSize().nestedScroll(nestedScroll),
        contentPadding = PaddingValues(start = margin, end = margin, top = topPadding + gap, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        item {
            SettingsGroup(cardShape) {
                Column {
                    SettingRow(tokens, Icons.Filled.Info, "TokiDiscover", trailing = {
                        Text(
                            "${BuildConfig.VERSION_NAME}（${BuildConfig.VERSION_CODE}）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    })
                    Row(
                        Modifier.fillMaxWidth().height(60.dp).clickable {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ichiyotsu/Tokidiscover")),
                            )
                        }.padding(horizontal = tokens.spaceDp("lg", 16)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.size(tokens.spaceDp("md", 12)))
                        Column(Modifier.weight(1f)) {
                            Text("TokiDiscover 仓库", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "浏览器的发版仓库",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        item {
            SettingsGroup(cardShape) {
                Column(
                    Modifier.padding(tokens.spaceDp("lg", 16)),
                    verticalArrangement = Arrangement.spacedBy(tokens.spaceDp("sm", 8)),
                ) {
                    Text("引擎是 Android 系统 WebView。", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "界面遵循 TokiUI ${tokens.version}。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "跟随系统"
    ThemeMode.Light -> "浅色"
    ThemeMode.Dark -> "深色"
}
