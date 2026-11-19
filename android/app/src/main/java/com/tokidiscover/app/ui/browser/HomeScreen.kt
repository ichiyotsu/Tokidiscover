package com.tokidiscover.app.ui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tokidiscover.app.data.DefaultSpeedDial
import com.tokidiscover.app.data.SpeedDialSite
import com.tokidiscover.app.ui.BrowserViewModel
import com.tokidiscover.app.ui.theme.TokiSpacing
import kotlin.math.absoluteValue

private val shortcutTileColors = listOf(
    0xFF7FB069.toInt(), 0xFF6A8DFF.toInt(), 0xFFE8A33D.toInt(),
    0xFF9B7EDE.toInt(), 0xFFE06C5B.toInt(), 0xFF4DB6AC.toInt(),
)

/**
 * Chrome-style new-tab page, restyled with TokiUI 0.1.10 tokens: a big search
 * pill on top, then the Speed Dial shortcuts row and the recent list. No
 * pill buttons under the search pill: Chrome's "AI 模式" is Chrome's own
 * feature and is deliberately not copied, and 无痕 lives in the ⋮ menu only.
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
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(TokiSpacing.cardSpacing),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(DefaultSpeedDial, key = { it.url }) { site ->
                        ShortcutTile(site) { vm.navigate(site.url) }
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

@Composable
private fun ShortcutTile(site: SpeedDialSite, onClick: () -> Unit) {
    val color = Color(shortcutTileColors[(site.title.hashCode().absoluteValue) % shortcutTileColors.size])
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(4.dp),
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                site.title.firstOrNull()?.toString().orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            site.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
