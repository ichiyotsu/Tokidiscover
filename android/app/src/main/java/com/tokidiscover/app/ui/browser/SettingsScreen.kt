package com.tokidiscover.app.ui.browser

import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
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
import com.tokiui.semantic.SemanticTokens
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    vm: BrowserViewModel,
    tokens: SemanticTokens,
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var engine by remember { mutableStateOf(vm.settings.engine()) }
    var template by remember { mutableStateOf(vm.settings.customTemplate) }
    var desktopUa by remember { mutableStateOf(vm.settings.desktopUa) }
    var javascript by remember { mutableStateOf(vm.settings.javaScriptEnabled) }
    var updateText by remember { mutableStateOf("尚未检查") }
    var checking by remember { mutableStateOf(false) }
    var pendingClear by remember { mutableStateOf<String?>(null) }
    val templateMissingQuery = engine == SearchEngine.Custom && !template.contains("{query}")
    Column(
        Modifier
            .fillMaxSize()
            .nestedScroll(nestedScroll)
            .verticalScroll(rememberScrollState())
            .padding(top = topPadding, bottom = bottomPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("搜索引擎", style = MaterialTheme.typography.titleMedium)
        Text("当前：${engine.displayName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        Text("主题", style = MaterialTheme.typography.titleMedium)
        Text(
            "当前：${themeLabel(themeMode)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(selected = themeMode == mode, onClick = { onThemeMode(mode) }, label = { Text(themeLabel(mode)) })
            }
        }
        ListItem(
            headlineContent = { Text("动态取色") },
            supportingContent = { Text(if (useDynamic) "使用系统壁纸的颜色" else "使用下面选中的 TokiUI 种子色") },
            trailingContent = { Switch(checked = useDynamic, onCheckedChange = onUseDynamic) },
        )
        if (!useDynamic) {
            Text("种子色：${tokens.seeds[seedIndex.coerceIn(tokens.seeds.indices)].name}", style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tokens.seeds.forEachIndexed { index, seed ->
                    FilterChip(selected = seedIndex == index, onClick = { onSeed(index) }, label = { Text(seed.name) })
                }
            }
        }
        ListItem(
            headlineContent = { Text("减少动效") },
            supportingContent = { Text(if (reduceMotion) "已改用标准动效" else "使用表现力动效") },
            trailingContent = { Switch(checked = reduceMotion, onCheckedChange = onReduceMotion) },
        )
        ListItem(
            headlineContent = { Text("玻璃模糊") },
            supportingContent = { Text(if (backdropBlur) "胶囊里采样窗口内容" else "胶囊改为不透明表面") },
            trailingContent = { Switch(checked = backdropBlur, onCheckedChange = onBackdropBlur) },
        )
        Text("浏览", style = MaterialTheme.typography.titleMedium)
        ListItem(
            headlineContent = { Text("桌面版 UA") },
            supportingContent = { Text(if (desktopUa) "正在使用桌面浏览器标识，刷新后生效" else "使用系统 WebView 标识，刷新后生效") },
            trailingContent = {
                Switch(checked = desktopUa, onCheckedChange = { desktopUa = it; vm.settings.desktopUa = it })
            },
        )
        ListItem(
            headlineContent = { Text("JavaScript") },
            supportingContent = { Text(if (javascript) "脚本已打开，刷新后生效" else "脚本已关闭，部分网站无法使用，刷新后生效") },
            trailingContent = {
                Switch(checked = javascript, onCheckedChange = { javascript = it; vm.settings.javaScriptEnabled = it })
            },
        )
        Text("清除浏览数据", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { pendingClear = "Cookie" }) { Text("Cookie") }
            OutlinedButton(onClick = { pendingClear = "缓存" }) { Text("缓存") }
            OutlinedButton(onClick = { pendingClear = "历史" }) { Text("历史") }
        }
        Text("关于", style = MaterialTheme.typography.titleMedium)
        Text("TokiDiscover ${BuildConfig.VERSION_NAME}（${BuildConfig.VERSION_CODE}）", style = MaterialTheme.typography.bodyLarge)
        Text(
            "引擎是 Android 系统 WebView。界面遵循 TokiUI ${tokens.version}。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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

private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "跟随系统"
    ThemeMode.Light -> "浅色"
    ThemeMode.Dark -> "深色"
}
