package com.tokidiscover.app.ui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.tokidiscover.app.data.SpeedDialSite
import com.tokidiscover.app.ui.BrowserViewModel
import com.tokidiscover.app.ui.theme.TokiSpacing

/**
 * New-tab page (TokiUI 0.1.11): a big search pill, then the Speed Dial
 * sites as pill chips in wrapping rows — no section titles, no recent
 * list. Long-press a site to delete it (with confirmation); the "+"
 * chip at the end adds a site by name + URL. Sites persist across
 * restarts (see BrowserViewModel.speedDial).
 */
@Composable
fun HomeScreen(
    vm: BrowserViewModel,
    topPadding: Dp,
    bottomPadding: Dp,
    nestedScroll: NestedScrollConnection,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<SpeedDialSite?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
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
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    vm.speedDial.forEach { site ->
                        ShortcutChip(
                            site = site,
                            onClick = { vm.navigate(site.url) },
                            onLongClick = { pendingDelete = site },
                        )
                    }
                    AddShortcutChip { showAddDialog = true }
                }
            }
        }
    }
    val deleting = pendingDelete
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除常用站点") },
            text = { Text("确定删除「${deleting.title}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    vm.removeSpeedDial(deleting)
                    pendingDelete = null
                }) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("取消") } },
        )
    }
    if (showAddDialog) {
        AddSpeedDialDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, url ->
                if (vm.addSpeedDial(title, url)) {
                    showAddDialog = false
                    null
                } else {
                    "网址无效或已存在"
                }
            },
        )
    }
}

/** A Speed Dial site as a pill chip: tap to open, long-press to delete. */
@Composable
private fun ShortcutChip(site: SpeedDialSite, onClick: () -> Unit, onLongClick: () -> Unit) {
    Box(
        Modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
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

/** The "+" chip at the end of the Speed Dial row. */
@Composable
private fun AddShortcutChip(onClick: () -> Unit) {
    Box(
        Modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Add, contentDescription = "添加常用站点", modifier = Modifier.padding(vertical = 12.dp))
    }
}

/** Add a Speed Dial site by name + URL. Returns an error message when the input is rejected. */
@Composable
private fun AddSpeedDialDialog(onDismiss: () -> Unit, onAdd: (String, String) -> String?) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加常用站点") },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("名字") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("网址") },
                    placeholder = { Text("例如 bilibili.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        error = onAdd(title, url)
                    }),
                    modifier = Modifier.fillMaxWidth(),
                )
                val message = error
                if (message != null) {
                    Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { error = onAdd(title, url) }) { Text("添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}
