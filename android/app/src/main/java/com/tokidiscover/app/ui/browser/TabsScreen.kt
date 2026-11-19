package com.tokidiscover.app.ui.browser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tokidiscover.app.data.BrowserTab
import com.tokidiscover.app.data.TabGroup
import com.tokidiscover.app.data.TabGroupColors
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.data.formatLastUpdated
import com.tokidiscover.app.ui.BrowserViewModel
import com.tokidiscover.app.ui.components.LongPressLabel
import com.tokidiscover.app.ui.theme.TokiSpacing
import kotlin.math.absoluteValue

private val letterTileColors = listOf(
    0xFF7FB069.toInt(), 0xFF6A8DFF.toInt(), 0xFFE8A33D.toInt(),
    0xFF9B7EDE.toInt(), 0xFFE06C5B.toInt(), 0xFF4DB6AC.toInt(),
)

private fun letterColorFor(text: String): Color =
    Color(letterTileColors[(text.hashCode().absoluteValue) % letterTileColors.size])

/** Chrome tab-grid top bar: + button, 标签/群组 segmented control, list toggle, ⋮ menu. */
@Composable
fun TabsScreen(
    vm: BrowserViewModel,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
    onPick: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var listMode by rememberSaveable { mutableStateOf(false) }
    var view by rememberSaveable { mutableIntStateOf(0) } // 0 标签页, 1 群组
    var overflowOpen by remember { mutableStateOf(false) }
    var expandedGroupId by rememberSaveable { mutableStateOf<String?>(null) }
    var addToGroupTab by remember { mutableStateOf<BrowserTab?>(null) }
    var renameTarget by remember { mutableStateOf<TabGroup?>(null) }
    var groupMenuId by remember { mutableStateOf<String?>(null) }

    val tabs = vm.tabs
    val groups = vm.groups
    val filteredTabs = remember(query, tabs) {
        if (query.isBlank()) tabs.toList() else tabs.filter {
            it.title.contains(query, ignoreCase = true) || it.url.contains(query, ignoreCase = true)
        }
    }
    val filteredGroups = remember(query, groups) {
        if (query.isBlank()) groups else groups.filter { it.name.contains(query, ignoreCase = true) }
    }
    val ungrouped = tabs.filter { tab -> groups.none { tab.id in it.tabIds } }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = topPadding, bottom = bottomPadding)
            .padding(horizontal = TokiSpacing.pageMargin),
    ) {
        // Top bar: + | 标签 N / 群组 M | list toggle | ⋮
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            LongPressLabel("新建标签页") {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { vm.newTab(); onPick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "新建标签页", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Row(
                Modifier
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SegmentButton("标签 ${tabs.size}", selected = view == 0) { view = 0 }
                SegmentButton("群组 ${groups.size}", selected = view == 1) { view = 1 }
            }
            Spacer(Modifier.weight(1f))
            LongPressLabel(if (listMode) "网格视图" else "列表视图") {
                IconButton(onClick = { listMode = !listMode }) {
                    Icon(
                        if (listMode) Icons.Filled.GridView else Icons.AutoMirrored.Filled.ViewList,
                        contentDescription = if (listMode) "网格视图" else "列表视图",
                    )
                }
            }
            Box {
                LongPressLabel("更多操作") {
                    IconButton(onClick = { overflowOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "更多操作")
                    }
                }
                DropdownMenu(expanded = overflowOpen, onDismissRequest = { overflowOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("新建标签页") },
                        onClick = { overflowOpen = false; vm.newTab(); onPick() },
                    )
                    DropdownMenuItem(
                        text = { Text("新建无痕标签页") },
                        onClick = { overflowOpen = false; vm.newTab(isPrivate = true); onPick() },
                    )
                    DropdownMenuItem(
                        text = { Text("关闭全部标签页") },
                        onClick = { overflowOpen = false; vm.closeAllTabs() },
                    )
                }
            }
        }
        // Search-tabs pill
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = TokiSpacing.cardSpacing)
                .height(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.CenterStart,
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text("搜索标签页") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, contentDescription = "清除搜索") } }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (view == 0) {
            TabsView(
                vm = vm,
                tabs = filteredTabs,
                listMode = listMode,
                showRecent = query.isBlank(),
                nestedScroll = nestedScroll,
                onPick = onPick,
                onLongPress = { addToGroupTab = it },
            )
        } else {
            GroupsView(
                vm = vm,
                groups = filteredGroups,
                ungroupedCount = ungrouped.size,
                expandedGroupId = expandedGroupId,
                onExpand = { expandedGroupId = if (expandedGroupId == it) null else it },
                groupMenuId = groupMenuId,
                onGroupMenu = { groupMenuId = it },
                onRename = { renameTarget = it },
                nestedScroll = nestedScroll,
                onPick = onPick,
                onNewGroup = {
                    val first = ungrouped.firstOrNull() ?: vm.activeTab
                    vm.createGroupForTab(first.id)
                },
            )
        }
    }

    addToGroupTab?.let { tab ->
        AddToGroupDialog(
            tab = tab,
            groups = vm.groups,
            onPickGroup = { groupId ->
                vm.addTabToGroup(tab.id, groupId)
                addToGroupTab = null
            },
            onNewGroup = {
                vm.createGroupForTab(tab.id)
                addToGroupTab = null
            },
            onDismiss = { addToGroupTab = null },
        )
    }
    renameTarget?.let { group ->
        RenameGroupDialog(
            group = group,
            onConfirm = { name ->
                vm.renameGroup(group.id, name)
                renameTarget = null
            },
            onDismiss = { renameTarget = null },
        )
    }
}

@Composable
private fun SegmentButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun ColumnScope.TabsView(
    vm: BrowserViewModel,
    tabs: List<BrowserTab>,
    listMode: Boolean,
    showRecent: Boolean,
    nestedScroll: NestedScrollConnection,
    onPick: () -> Unit,
    onLongPress: (BrowserTab) -> Unit,
) {
    if (listMode) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(TokiSpacing.cardSpacing),
            modifier = Modifier.weight(1f).nestedScroll(nestedScroll),
        ) {
            recentItems(vm, showRecent)
            items(tabs, key = { it.id }) { tab ->
                TabRow(vm, tab, onPick, onLongPress)
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(TokiSpacing.cardSpacing),
            verticalArrangement = Arrangement.spacedBy(TokiSpacing.cardSpacing),
            modifier = Modifier.weight(1f).nestedScroll(nestedScroll),
        ) {
            recentItems(vm, showRecent)
            items(tabs, key = { it.id }) { tab ->
                TabCard(vm, tab, onPick, onLongPress)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.recentItems(
    vm: BrowserViewModel,
    showRecent: Boolean,
) {
    if (!showRecent || vm.recentlyClosed.isEmpty()) return
    item {
        Text(
            "最近关闭",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
    items(vm.recentlyClosed, key = { it.url }) { closed ->
        Row(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable { vm.restoreClosedTab(closed) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                closed.title.ifBlank { closed.url },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            LongPressLabel("恢复此标签页") {
                IconButton(onClick = { vm.restoreClosedTab(closed) }) {
                    Icon(Icons.Filled.Restore, contentDescription = "恢复此标签页")
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.grid.LazyGridScope.recentItems(
    vm: BrowserViewModel,
    showRecent: Boolean,
) {
    if (!showRecent || vm.recentlyClosed.isEmpty()) return
    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
        Text(
            "最近关闭",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
    items(vm.recentlyClosed, key = { it.url }) { closed ->
        Row(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable { vm.restoreClosedTab(closed) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                closed.title.ifBlank { closed.url },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            LongPressLabel("恢复此标签页") {
                IconButton(onClick = { vm.restoreClosedTab(closed) }) {
                    Icon(Icons.Filled.Restore, contentDescription = "恢复此标签页")
                }
            }
        }
    }
}

@Composable
private fun TabCard(
    vm: BrowserViewModel,
    tab: BrowserTab,
    onPick: () -> Unit,
    onLongPress: (BrowserTab) -> Unit,
) {
    val active = tab.id == vm.activeTabId
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.combinedClickable(
            onClick = { vm.selectTab(tab.id); onPick() },
            onLongClick = { onLongPress(tab) },
        ),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 4.dp),
            ) {
                Favicon(tab, 20.dp)
                Text(
                    tab.title.ifBlank { "新标签页" },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                )
                LongPressLabel("关闭标签") {
                    IconButton(onClick = { vm.closeTab(tab.id) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "关闭标签", modifier = Modifier.size(18.dp))
                    }
                }
            }
            val thumb = tab.thumbnail
            if (thumb != null) {
                Image(
                    bitmap = thumb.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Favicon(tab, 48.dp)
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun TabRow(
    vm: BrowserViewModel,
    tab: BrowserTab,
    onPick: () -> Unit,
    onLongPress: (BrowserTab) -> Unit,
) {
    val active = tab.id == vm.activeTabId
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
            .combinedClickable(
                onClick = { vm.selectTab(tab.id); onPick() },
                onLongClick = { onLongPress(tab) },
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Favicon(tab, 24.dp)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                tab.title.ifBlank { "新标签页" },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                if (tab.url == UrlNormalizer.NEW_TAB_URL) "新标签页" else tab.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        LongPressLabel("关闭标签") {
            IconButton(onClick = { vm.closeTab(tab.id) }) {
                Icon(Icons.Filled.Close, contentDescription = "关闭标签")
            }
        }
    }
}

@Composable
private fun ColumnScope.GroupsView(
    vm: BrowserViewModel,
    groups: List<TabGroup>,
    ungroupedCount: Int,
    expandedGroupId: String?,
    onExpand: (String) -> Unit,
    groupMenuId: String?,
    onGroupMenu: (String?) -> Unit,
    onRename: (TabGroup) -> Unit,
    nestedScroll: NestedScrollConnection,
    onPick: () -> Unit,
    onNewGroup: () -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(TokiSpacing.cardSpacing),
        modifier = Modifier.weight(1f).nestedScroll(nestedScroll),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "共 ${groups.size} 个群组",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilledTonalButton(
                    onClick = onNewGroup,
                    enabled = ungroupedCount > 0,
                    modifier = Modifier.height(48.dp),
                ) { Text("新建群组") }
            }
        }
        if (groups.isEmpty()) {
            item {
                Text(
                    "还没有标签页群组。长按标签页卡片，选择「加入群组」即可创建。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        }
        items(groups, key = { it.id }) { group ->
            GroupCard(
                vm = vm,
                group = group,
                expanded = expandedGroupId == group.id,
                onExpand = { onExpand(group.id) },
                menuOpen = groupMenuId == group.id,
                onMenu = { onGroupMenu(if (groupMenuId == group.id) null else group.id) },
                onRename = { onRename(group) },
                onPick = onPick,
            )
        }
    }
}

@Composable
private fun GroupCard(
    vm: BrowserViewModel,
    group: TabGroup,
    expanded: Boolean,
    onExpand: () -> Unit,
    menuOpen: Boolean,
    onMenu: () -> Unit,
    onRename: () -> Unit,
    onPick: () -> Unit,
) {
    val color = Color(TabGroupColors[group.colorIndex % TabGroupColors.size])
    val groupTabs = group.tabIds.mapNotNull { id -> vm.tabs.firstOrNull { it.id == id } }
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(Modifier.padding(TokiSpacing.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FaviconGrid(tabs = groupTabs)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
                        Text(
                            group.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Text(
                        "${groupTabs.size} 个标签 · 上次更新时间：${formatLastUpdated(System.currentTimeMillis(), group.updatedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Box {
                    LongPressLabel("群组操作") {
                        IconButton(onClick = onMenu) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "群组操作")
                        }
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = onMenu) {
                        DropdownMenuItem(text = { Text("重命名") }, onClick = { onMenu(); onRename() })
                        DropdownMenuItem(
                            text = { Text("解散群组") },
                            onClick = { onMenu(); vm.deleteGroup(group.id) },
                        )
                        DropdownMenuItem(
                            text = { Text("关闭群组") },
                            onClick = { onMenu(); vm.closeGroup(group.id) },
                        )
                    }
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider()
                    groupTabs.forEach { tab ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { vm.selectTab(tab.id); onPick() }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Favicon(tab, 20.dp)
                            Text(
                                tab.title.ifBlank { "新标签页" },
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                            )
                            LongPressLabel("关闭标签") {
                                IconButton(onClick = { vm.closeTab(tab.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Filled.Close, contentDescription = "关闭标签", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
            TextButton(
                onClick = onExpand,
                modifier = Modifier.padding(top = 4.dp),
            ) { Text(if (expanded) "收起" else "查看标签") }
        }
    }
}

/** 2x2 favicon grid for a group card, like Chrome's group icon. */
@Composable
private fun FaviconGrid(tabs: List<BrowserTab>) {
    Box(
        Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                FaviconCell(tabs.getOrNull(0))
                FaviconCell(tabs.getOrNull(1))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                FaviconCell(tabs.getOrNull(2))
                FaviconCell(tabs.getOrNull(3))
            }
        }
    }
}

@Composable
private fun FaviconCell(tab: BrowserTab?) {
    Box(
        Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (tab != null) Favicon(tab, 20.dp)
    }
}

/** A tab's favicon, or a letter tile when the page never reported one. */
@Composable
private fun Favicon(tab: BrowserTab, size: Dp) {
    val icon = tab.favicon
    if (icon != null) {
        Image(
            bitmap = icon.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        val letter = tab.title.firstOrNull()?.toString().orEmpty().ifBlank { "新" }
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(letterColorFor(tab.title.ifBlank { tab.url })),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                letter,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun AddToGroupDialog(
    tab: BrowserTab,
    groups: List<TabGroup>,
    onPickGroup: (String) -> Unit,
    onNewGroup: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("加入群组") },
        text = {
            Column {
                groups.forEach { group ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPickGroup(group.id) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(TabGroupColors[group.colorIndex % TabGroupColors.size])),
                        )
                        Text(group.name, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.weight(1f))
                        Text("${group.tabIds.size} 个", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNewGroup() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("新建群组", modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun RenameGroupDialog(
    group: TabGroup,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(group.id) { mutableStateOf(group.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名群组") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(name) }) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}
