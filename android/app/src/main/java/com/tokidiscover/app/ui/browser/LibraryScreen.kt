package com.tokidiscover.app.ui.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tokidiscover.app.ui.BrowserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val sections = listOf("书签", "历史", "下载")

@Composable
fun LibraryScreen(
    vm: BrowserViewModel,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
    onOpenUrl: (String) -> Unit,
) {
    var section by rememberSaveable { mutableIntStateOf(0) }
    val format = rememberSaveableFormat()
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = topPadding, bottom = bottomPadding)
            .padding(horizontal = 16.dp),
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            sections.forEachIndexed { index, label ->
                FilterChip(selected = section == index, onClick = { section = index }, label = { Text(label) })
            }
        }
        Text(
            "正在查看${sections[section]}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        Box(Modifier.weight(1f).nestedScroll(nestedScroll)) {
            when (section) {
                0 -> if (vm.bookmarks.isEmpty()) {
                    Note("暂无书签", "在页面菜单里点「收藏」。书签放在「默认」文件夹。")
                } else {
                    LazyColumn {
                        items(vm.bookmarks, key = { it.id }) { bookmark ->
                            ListItem(
                                headlineContent = { Text(bookmark.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                supportingContent = { Text("${bookmark.folder} · ${bookmark.url}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                trailingContent = {
                                    IconButton(onClick = { vm.removeBookmark(bookmark.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "删除书签")
                                    }
                                },
                                modifier = Modifier.clickable { onOpenUrl(bookmark.url) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
                1 -> if (vm.history.isEmpty()) {
                    Note("暂无历史", "无痕标签页不会产生历史记录。")
                } else {
                    LazyColumn {
                        item {
                            TextButton(onClick = { vm.clearHistory() }) { Text("清空历史") }
                        }
                        items(vm.history, key = { it.id }) { entry ->
                            ListItem(
                                headlineContent = { Text(entry.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                supportingContent = {
                                    Text(
                                        "${format.format(Date(entry.visitedAt))} · ${entry.url}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                modifier = Modifier.clickable { onOpenUrl(entry.url) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
                else -> if (vm.downloads.isEmpty()) {
                    Note("暂无下载", "网页触发的下载会交给系统「下载」应用，并在这里留下记录。")
                } else {
                    LazyColumn {
                        items(vm.downloads, key = { it.id }) { download ->
                            ListItem(
                                headlineContent = { Text(download.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                supportingContent = {
                                    Text(
                                        "${download.status} · ${if (download.totalBytes > 0) "${download.totalBytes / 1024} KB" else "大小未知"}",
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberSaveableFormat(): SimpleDateFormat {
    return androidx.compose.runtime.remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
}

@Composable
private fun Note(title: String, body: String) {
    Column(Modifier.padding(top = 24.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
