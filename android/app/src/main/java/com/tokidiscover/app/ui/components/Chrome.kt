package com.tokidiscover.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DropdownMenu
import com.tokidiscover.app.data.BrowserTab
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.ui.Dest

data class NavEntry(val dest: Dest, val icon: ImageVector, val selectedIcon: ImageVector, val label: String)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.pressScale(interaction: MutableInteractionSource, scale: Float = 0.92f): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val animated by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) scale else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = animated
        scaleY = animated
    }
}

@Composable
fun OverlayTitle(title: String, overlapped: Boolean, onHeight: (Dp) -> Unit, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val chip by animateColorAsState(
        if (overlapped) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "titleChip",
    )
    val content by animateColorAsState(
        if (overlapped) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "titleContent",
    )
    Box(
        modifier
            .fillMaxWidth()
            .onSizeChanged { onHeight(with(density) { it.height.toDp() }) }
            .statusBarsPadding()
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(chip)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverNavBar(
    entries: List<NavEntry>,
    current: Dest,
    onSelect: (Dest) -> Unit,
    glass: Modifier,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(percent = 50))
            .then(glass)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        entries.forEach { entry ->
            NavItem(entry, current == entry.dest) { onSelect(entry.dest) }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NavItem(entry: NavEntry, selected: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .height(56.dp)
            .clip(CircleShape)
            .background(if (selected) scheme.primary else Color.Transparent)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = scheme.primary.copy(alpha = 0.28f)),
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .pressScale(interaction)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) entry.selectedIcon else entry.icon,
            contentDescription = entry.label,
            tint = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(MaterialTheme.motionScheme.fastSpatialSpec()) +
                fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = shrinkHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(8.dp))
                Text(entry.label, style = MaterialTheme.typography.labelLarge, color = scheme.onPrimary, maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverOmnibox(
    tab: BrowserTab,
    collapsed: Boolean,
    tabCount: Int,
    bookmarked: Boolean,
    glass: Modifier,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onTabs: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onFind: () -> Unit,
    onTranslate: () -> Unit,
    onEditing: (Boolean) -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember(tab.id) { mutableStateOf(false) }
    var draft by remember(tab.id) { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    val loading = tab.progress in 1..99
    val host = tab.url.removePrefix("https://").removePrefix("http://").substringBefore('/')
    val motion = MaterialTheme.motionScheme
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 50))
            .then(glass),
    ) {
        if (loading) {
            LinearProgressIndicator(
                progress = { tab.progress / 100f },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent,
            )
        }
        AnimatedContent(
            targetState = collapsed && !editing,
            transitionSpec = {
                fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.defaultEffectsSpec())
            },
            label = "omnibox",
        ) { compact ->
            if (compact) {
                Row(
                    Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (tab.isPrivate) "无痕 · ${tab.title.ifBlank { host }}" else tab.title.ifBlank { host },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .clickable {
                                onExpand()
                                editing = true
                                draft = if (tab.url == UrlNormalizer.NEW_TAB_URL) "" else tab.url
                                onEditing(true)
                            }
                            .padding(horizontal = 12.dp),
                    )
                    TabCountButton(tabCount, onTabs)
                    PageMenu(
                        expanded = menu,
                        bookmarked = bookmarked,
                        onExpanded = { menu = it },
                        onBookmark = onBookmark,
                        onShare = onShare,
                        onFind = onFind,
                        onTranslate = onTranslate,
                    )
                }
            } else {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, enabled = tab.canGoBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "后退")
                    }
                    IconButton(onClick = onForward, enabled = tab.canGoForward) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "前进")
                    }
                    if (editing) {
                        androidx.compose.material3.TextField(
                            value = draft,
                            onValueChange = { draft = it },
                            singleLine = true,
                            placeholder = { Text("搜索或输入网址") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                onSubmit(draft)
                                editing = false
                                onEditing(false)
                            }),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .clickable {
                                    editing = true
                                    draft = if (tab.url == UrlNormalizer.NEW_TAB_URL) "" else tab.url
                                    onEditing(true)
                                }
                                .padding(horizontal = 8.dp),
                        ) {
                            Text(
                                if (tab.isPrivate) "无痕 · ${tab.title.ifBlank { "新标签页" }}" else tab.title.ifBlank { "新标签页" },
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                if (tab.url == UrlNormalizer.NEW_TAB_URL) "点此搜索或输入网址" else tab.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    IconButton(onClick = onReloadOrStop) {
                        Icon(
                            if (loading) Icons.Filled.Close else Icons.Filled.Refresh,
                            contentDescription = if (loading) "停止" else "刷新",
                        )
                    }
                    PageMenu(
                        expanded = menu,
                        bookmarked = bookmarked,
                        onExpanded = { menu = it },
                        onBookmark = onBookmark,
                        onShare = onShare,
                        onFind = onFind,
                        onTranslate = onTranslate,
                    )
                    TabCountButton(tabCount, onTabs)
                }
            }
        }
    }
}

@Composable
private fun TabCountButton(tabCount: Int, onTabs: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .height(48.dp)
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = ripple()) { onTabs() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Tab,
            contentDescription = "标签页，共 $tabCount 个",
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text("$tabCount", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PageMenu(
    expanded: Boolean,
    bookmarked: Boolean,
    onExpanded: (Boolean) -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onFind: () -> Unit,
    onTranslate: () -> Unit,
) {
    Box {
        IconButton(onClick = { onExpanded(true) }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "页面菜单")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpanded(false) }) {
            DropdownMenuItem(
                text = { Text(if (bookmarked) "已收藏" else "收藏") },
                leadingIcon = {
                    Icon(if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = null)
                },
                onClick = { onExpanded(false); onBookmark() },
            )
            DropdownMenuItem(
                text = { Text("页内查找") },
                leadingIcon = { Icon(Icons.Outlined.FindInPage, contentDescription = null) },
                onClick = { onExpanded(false); onFind() },
            )
            DropdownMenuItem(
                text = { Text("分享") },
                leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                onClick = { onExpanded(false); onShare() },
            )
            DropdownMenuItem(
                text = { Text("翻译此页") },
                leadingIcon = { Icon(Icons.Outlined.Translate, contentDescription = null) },
                onClick = { onExpanded(false); onTranslate() },
            )
        }
    }
}
