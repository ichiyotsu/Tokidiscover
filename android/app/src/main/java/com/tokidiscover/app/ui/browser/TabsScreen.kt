package com.tokidiscover.app.ui.browser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tokidiscover.app.ui.theme.TokiSpacing
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.ui.BrowserViewModel
import com.tokidiscover.app.ui.components.LongPressLabel

@Composable
fun TabsScreen(
    vm: BrowserViewModel,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
    onPick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = topPadding, bottom = bottomPadding)
            .padding(horizontal = TokiSpacing.pageMargin),
    ) {
        Text(
            "共 ${vm.tabs.size} 个标签，当前是「${vm.activeTab.title}」",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            horizontalArrangement = Arrangement.spacedBy(TokiSpacing.cardSpacing),
            verticalArrangement = Arrangement.spacedBy(TokiSpacing.cardSpacing),
            modifier = Modifier.weight(1f).nestedScroll(nestedScroll).padding(vertical = TokiSpacing.cardSpacing),
        ) {
            items(vm.tabs, key = { it.id }) { tab ->
                val active = tab.id == vm.activeTabId
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column(Modifier.padding(TokiSpacing.cardPadding)) {
                        Row {
                            Text(
                                tab.title.ifBlank { "新标签页" },
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            LongPressLabel("关闭标签") {
                                IconButton(onClick = { vm.closeTab(tab.id) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "关闭标签")
                                }
                            }
                        }
                        Text(
                            when {
                                tab.isPrivate && tab.url == UrlNormalizer.NEW_TAB_URL -> "无痕 · 新标签页"
                                tab.isPrivate -> "无痕 · ${tab.url}"
                                tab.url == UrlNormalizer.NEW_TAB_URL -> "新标签页"
                                else -> tab.url
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        TextButton(onClick = { vm.selectTab(tab.id); onPick() }) {
                            Text(if (active) "当前标签" else "切换到此标签")
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(TokiSpacing.cardSpacing)) {
            Button(onClick = { vm.newTab(); onPick() }, modifier = Modifier.height(56.dp)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("新建标签", modifier = Modifier.padding(start = 6.dp))
            }
            OutlinedButton(onClick = { vm.newTab(isPrivate = true); onPick() }, modifier = Modifier.height(56.dp)) { Text("新建无痕标签") }
        }
    }
}
