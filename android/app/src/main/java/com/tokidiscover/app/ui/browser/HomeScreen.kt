package com.tokidiscover.app.ui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tokidiscover.app.data.DefaultSpeedDial
import com.tokidiscover.app.data.SpeedDialSite
import com.tokidiscover.app.ui.BrowserViewModel
import com.tokidiscover.app.ui.theme.TokiSpacing

/**
 * Chrome-style new-tab page, restyled with TokiUI 0.1.11 tokens: the title
 * lives in the unified frosted header pill ("TokiDiscover", top-left), then
 * a big search pill, then the Speed Dial shortcuts as pill chips in
 * wrapping rows, then the recent list. No pill buttons under the search
 * pill: Chrome's "AI 模式" is Chrome's own feature and is deliberately
 * not copied, and 无痕 lives in the ⋮ menu only.
 */
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
            .padding(horizontal = TokiSpacing.pageMargin),
        verticalArrangement = Arrangement.spacedBy(TokiSpacing.cardSpacing),
    ) {
        if (vm.activeTab.isPrivate) {
            item {
                Text(
                    "这个无痕标签页不会写入历史",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            // Big search pill: tapping focuses it, Go navigates.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.CenterStart,
            ) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text("搜索或输入网址") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { vm.submitOmnibox(query) }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (vm.settings.showSpeedDial) {
            item { Text("常用站点", style = MaterialTheme.typography.titleMedium) }
            item {
                // Shortcut pills in wrapping rows (was a horizontal-scrolling
                // row of round letter tiles): same sites, same navigation.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    DefaultSpeedDial.forEach { site ->
                        ShortcutChip(site) { vm.navigate(site.url) }
                    }
                }
            }
        }
        if (!vm.activeTab.isPrivate && vm.settings.showRecentOnNewTab) {
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

/** A Speed Dial site as a pill chip: site name on surfaceContainerHigh. */
@Composable
private fun ShortcutChip(site: SpeedDialSite, onClick: () -> Unit) {
    Box(
        Modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            site.title,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
