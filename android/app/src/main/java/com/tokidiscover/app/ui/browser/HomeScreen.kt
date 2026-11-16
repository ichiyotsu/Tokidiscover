package com.tokidiscover.app.ui.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tokidiscover.app.data.DefaultSpeedDial
import com.tokidiscover.app.ui.BrowserViewModel

@Composable
fun HomeScreen(
    vm: BrowserViewModel,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
) {
    var query by rememberSaveable { mutableStateOf("") }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .nestedScroll(nestedScroll)
            .padding(top = topPadding, bottom = bottomPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                if (vm.activeTab.isPrivate) "这个无痕标签页不会写入历史" else "搜索或输入网址",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            TextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                label = { Text("搜索或输入网址") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { vm.submitOmnibox(query) }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { Text("常用站点", style = MaterialTheme.typography.titleMedium) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DefaultSpeedDial.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { site ->
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { vm.navigate(site.url) },
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(site.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    Text(
                                        site.url.removePrefix("https://"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!vm.activeTab.isPrivate) {
            item { Text("最近访问", style = MaterialTheme.typography.titleMedium) }
            if (vm.history.isEmpty()) {
                item {
                    Text(
                        "访问过的页面会显示在这里",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(vm.history.take(8), key = { it.id }) { entry ->
                    ListItem(
                        headlineContent = { Text(entry.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        supportingContent = { Text(entry.url, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.clickable { vm.navigate(entry.url) },
                    )
                }
            }
        }
    }
}
