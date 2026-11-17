package com.tokidiscover.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tokidiscover.app.data.BrowserTab
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.ui.CapsuleDock
import com.tokidiscover.app.ui.Dest
import com.tokidiscover.app.ui.capsuleOffsetFor
import com.tokidiscover.app.ui.nearestCapsuleDock
import com.tokidiscover.app.ui.theme.TokiChromeStroke
import com.tokidiscover.app.ui.theme.TokiScrollChipBackdrop
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

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
        clip = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongPressLabel(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content,
    )
}

@Composable
fun OverlayTitle(
    title: String,
    overlapped: Boolean,
    backdropBlur: Boolean,
    hazeState: HazeState,
    onHeight: (Dp) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val density = LocalDensity.current
    val cover by animateFloatAsState(if (overlapped) 1f else 0f, label = "scrollChip")
    val shape = RoundedCornerShape(percent = 50)
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
                .clip(shape),
            contentAlignment = Alignment.CenterStart,
        ) {
            TokiScrollChipBackdrop(
                shape = shape,
                cover = cover,
                backdropBlur = backdropBlur,
                hazeState = hazeState,
                applyBlur = true,
                modifier = Modifier.matchParentSize(),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
                Text(
                    title,
                    modifier = Modifier.padding(start = if (onBack != null) 0.dp else 16.dp, end = 16.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * The destination row (浏览 / 标签 / 书库 / 设置). It no longer has its own
 * capsule: it lives inside the "更多" menu panel, which owns the glass.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverNavBar(
    entries: List<NavEntry>,
    current: Dest,
    onSelect: (Dest) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
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
    LongPressLabel(entry.label) {
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
}

/** The "更多" secondary menu: navigation entries plus page actions, on one glass panel. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MoreMenuPanel(
    entries: List<NavEntry>,
    current: Dest,
    onSelect: (Dest) -> Unit,
    bookmarked: Boolean,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onFind: () -> Unit,
    onTranslate: () -> Unit,
    glass: Modifier,
    solidStroke: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .then(glass)
            .then(
                if (solidStroke) {
                    Modifier.border(TokiChromeStroke.width, MaterialTheme.colorScheme.primary, shape)
                } else {
                    Modifier
                },
            )
            .padding(vertical = 8.dp),
    ) {
        DiscoverNavBar(entries, current, onSelect, Modifier.fillMaxWidth())
        HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        MenuActionRow(
            if (bookmarked) "已收藏" else "收藏",
            if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            onBookmark,
        )
        MenuActionRow("页内查找", Icons.Outlined.FindInPage, onFind)
        MenuActionRow("分享", Icons.Outlined.Share, onShare)
        MenuActionRow("翻译此页", Icons.Outlined.Translate, onTranslate)
    }
}

@Composable
private fun MenuActionRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

private enum class OmniboxState { Expanded, Compact, Capsule }

/**
 * The bottom chrome. Browsing is the home: on compact widths this bar is
 * always present. [compactMode] shows the slim row on the new-tab home and
 * the other destinations; scrolling down on a page shrinks it to the
 * draggable [Capsule] instead. Editing always forces [Expanded].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverOmnibox(
    tab: BrowserTab,
    collapsed: Boolean,
    compactMode: Boolean,
    editRequest: Int,
    reduceMotion: Boolean,
    tabCount: Int,
    glass: Modifier,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onTabs: () -> Unit,
    onMenuClick: () -> Unit,
    onAddressTap: () -> Unit,
    onEditing: (Boolean) -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember(tab.id) { mutableStateOf(false) }
    var draft by remember(tab.id) { mutableStateOf("") }
    val loading = tab.progress in 1..99
    val host = tab.url.removePrefix("https://").removePrefix("http://").substringBefore('/')
    val motion = MaterialTheme.motionScheme

    // Capsule dock state lives above AnimatedContent so a drag offset survives
    // the expand/collapse transitions. The dock persists across recreation.
    var dockName by rememberSaveable { mutableStateOf(CapsuleDock.Center.name) }
    val dock = runCatching { CapsuleDock.valueOf(dockName) }.getOrDefault(CapsuleDock.Center)
    val capsuleOffset = remember { Animatable(0f) }
    val dragScope = rememberCoroutineScope()

    LaunchedEffect(editRequest) {
        if (editRequest > 0) {
            editing = true
            draft = if (tab.url == UrlNormalizer.NEW_TAB_URL) "" else tab.url
            onEditing(true)
        }
    }

    val state = when {
        editing -> OmniboxState.Expanded
        compactMode -> OmniboxState.Compact
        collapsed -> OmniboxState.Capsule
        else -> OmniboxState.Expanded
    }

    Column(modifier.fillMaxWidth()) {
        if (loading) {
            LinearProgressIndicator(
                progress = { tab.progress / 100f },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent,
            )
        }
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.defaultEffectsSpec())
            },
            label = "omnibox",
        ) { current ->
            when (current) {
                OmniboxState.Compact -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(CircleShape)
                            .then(glass)
                            .padding(horizontal = 8.dp),
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
                                .clickable { onAddressTap() }
                                .padding(horizontal = 12.dp),
                        )
                        TabCountButton(tabCount, onTabs)
                        LongPressLabel("页面菜单") {
                            IconButton(onClick = onMenuClick) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "页面菜单")
                            }
                        }
                    }
                }
                OmniboxState.Capsule -> {
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val containerWidth = maxWidth
                        // Small capsule: 60% of the bar width, clamped so it stays
                        // tappable on narrow phones and small on wide screens.
                        val capsuleWidth = (containerWidth * 0.6f).coerceIn(180.dp, 280.dp)
                        val density = LocalDensity.current
                        val containerPx = with(density) { containerWidth.toPx() }
                        val capsulePx = with(density) { capsuleWidth.toPx() }
                        val maxOffset = (containerPx - capsulePx).coerceAtLeast(0f)
                        LaunchedEffect(containerPx, capsulePx) {
                            capsuleOffset.snapTo(capsuleOffsetFor(dock, containerPx, capsulePx))
                        }
                        Box(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier
                                    .width(capsuleWidth)
                                    .offset { IntOffset(capsuleOffset.value.roundToInt(), 0) }
                                    .height(48.dp)
                                    .clip(CircleShape)
                                    .then(glass)
                                    .clickable { onExpand() }
                                    .draggable(
                                        state = rememberDraggableState { delta ->
                                            dragScope.launch {
                                                capsuleOffset.snapTo(
                                                    (capsuleOffset.value + delta).coerceIn(0f, maxOffset),
                                                )
                                            }
                                        },
                                        orientation = Orientation.Horizontal,
                                        onDragStopped = { velocity ->
                                            val target = nearestCapsuleDock(
                                                capsuleOffset.value,
                                                containerPx,
                                                capsulePx,
                                                velocity,
                                            )
                                            dockName = target.name
                                            val targetOffset = capsuleOffsetFor(target, containerPx, capsulePx)
                                            dragScope.launch {
                                                if (reduceMotion) {
                                                    capsuleOffset.snapTo(targetOffset)
                                                } else {
                                                    capsuleOffset.animateTo(
                                                        targetOffset,
                                                        motion.defaultSpatialSpec(),
                                                    )
                                                }
                                            }
                                        },
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = if (tab.isPrivate) "无痕 · ${tab.title.ifBlank { host }}" else tab.title.ifBlank { host },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 16.dp),
                                )
                                LongPressLabel("页面菜单") {
                                    IconButton(onClick = onMenuClick) {
                                        Icon(Icons.Filled.MoreVert, contentDescription = "页面菜单")
                                    }
                                }
                            }
                        }
                    }
                }
                OmniboxState.Expanded -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .then(glass)
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LongPressLabel("后退") {
                            IconButton(onClick = onBack, enabled = tab.canGoBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "后退")
                            }
                        }
                        LongPressLabel("前进") {
                            IconButton(onClick = onForward, enabled = tab.canGoForward) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "前进")
                            }
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
                        LongPressLabel(if (loading) "停止" else "刷新") {
                            IconButton(onClick = onReloadOrStop) {
                                Icon(
                                    if (loading) Icons.Filled.Close else Icons.Filled.Refresh,
                                    contentDescription = if (loading) "停止" else "刷新",
                                )
                            }
                        }
                        LongPressLabel("页面菜单") {
                            IconButton(onClick = onMenuClick) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "页面菜单")
                            }
                        }
                        TabCountButton(tabCount, onTabs)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabCountButton(tabCount: Int, onTabs: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val label = "标签页，共 $tabCount 个"
    LongPressLabel(label) {
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
            contentDescription = label,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text("$tabCount", style = MaterialTheme.typography.labelLarge)
    }
    }
}
