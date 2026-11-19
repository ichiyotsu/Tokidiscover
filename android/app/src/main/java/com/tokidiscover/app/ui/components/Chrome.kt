package com.tokidiscover.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tokidiscover.app.data.BrowserTab
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.ui.BottomBarState
import com.tokidiscover.app.ui.CapsuleDock
import com.tokidiscover.app.ui.Dest
import com.tokidiscover.app.ui.bottomBarState
import com.tokidiscover.app.ui.capsuleOffsetFor
import com.tokidiscover.app.ui.mainDomain
import com.tokidiscover.app.ui.nearestCapsuleDock
import com.tokidiscover.app.ui.theme.TokiChromeStroke
import com.tokidiscover.app.ui.theme.TokiScrollChipBackdrop
import com.tokidiscover.app.ui.theme.TokiSpacing
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

/**
 * The one title header for the whole app (TokiStudio 0.1.10): a single text
 * role (headlineMedium, Bold), a 56dp round back chip, a fixed 12dp gap.
 * The frost is always on — it never fades in or out with scroll.
 */
@Composable
fun OverlayTitleHeader(
    title: String,
    onBack: (() -> Unit)?,
    onHeightMeasured: (Dp) -> Unit,
    backdropBlur: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val content = MaterialTheme.colorScheme.onSurface
    val titleShape = RoundedCornerShape(percent = 50)
    Row(
        modifier
            .fillMaxWidth()
            .onSizeChanged { onHeightMeasured(with(density) { it.height.toDp() }) }
            .statusBarsPadding()
            .heightIn(min = 64.dp)
            .padding(horizontal = TokiSpacing.pageMargin),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            TokiScrollBackChip(
                backdropBlur = backdropBlur,
                hazeState = hazeState,
                onClick = onBack,
            )
            Spacer(Modifier.width(TokiSpacing.md))
        }
        Box(
            Modifier
                .weight(1f, fill = false)
                .heightIn(min = 56.dp)
                .clip(titleShape),
            contentAlignment = Alignment.CenterStart,
        ) {
            TokiScrollChipBackdrop(
                shape = titleShape,
                backdropBlur = backdropBlur,
                hazeState = hazeState,
                modifier = Modifier.matchParentSize(),
            )
            Text(
                title,
                modifier = Modifier.padding(horizontal = TokiSpacing.titleGlyphInset),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The back chip: a 56dp round frosted chip, same frost as the title. */
@Composable
fun TokiScrollBackChip(
    backdropBlur: Boolean,
    hazeState: HazeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val content = MaterialTheme.colorScheme.onSurface
    LongPressLabel("返回", modifier) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable(interactionSource = interaction, indication = ripple()) { onClick() }
                .pressScale(interaction),
            contentAlignment = Alignment.Center,
        ) {
            TokiScrollChipBackdrop(
                shape = CircleShape,
                backdropBlur = backdropBlur,
                hazeState = hazeState,
                modifier = Modifier.matchParentSize(),
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = content,
                modifier = Modifier.size(24.dp),
            )
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

/**
 * The regular floating bottom bar (TokiStudio 0.1.10): one glass capsule,
 * 56dp items, the selected item expands into a solid pill. The glass is
 * content-source sampled (the capsule width animates) and the row carries
 * a clip guard: overshoot may only reach 8dp into the gap beside the bar,
 * never under a neighbouring button. The glass box is the only
 * matchParentSize child — it does not take part in measuring, so the row
 * alone defines the capsule size (0.1.7: two such children collapsed it).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverFloatingNavBar(
    entries: List<NavEntry>,
    current: Dest,
    onSelect: (Dest) -> Unit,
    glass: Modifier,
    modifier: Modifier = Modifier,
    solidStroke: Boolean = false,
) {
    val shape = RoundedCornerShape(percent = 50)
    val inset = if (solidStroke) TokiChromeStroke.width + TokiChromeStroke.barGap else 8.dp
    Box(
        modifier = modifier
            .animateContentSize(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .clip(shape)
                .then(glass)
                .then(
                    if (solidStroke) {
                        Modifier.border(TokiChromeStroke.width, MaterialTheme.colorScheme.primary, shape)
                    } else {
                        Modifier
                    },
                ),
        )
        Row(
            modifier = Modifier
                .padding(horizontal = inset, vertical = inset)
                .drawWithContent {
                    val slack = 8.dp.toPx()
                    clipRect(left = -slack, top = 0f, right = size.width + slack, bottom = size.height) {
                        this@drawWithContent.drawContent()
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            entries.forEach { entry ->
                NavItem(entry, current == entry.dest) { onSelect(entry.dest) }
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

/**
 * The bottom bar in its three shapes (TokiStudio 0.1.10 redesign):
 * - [BottomBarState.Nav]: the regular floating glass capsule nav, on the
 *   home page and the app destinations.
 * - [BottomBarState.FunctionBar]: on a website — outer glass capsule,
 *   inner pill with the full URL, back/forward/reload/tabs/menu around it.
 *   Tapping the pill swaps it for the address field.
 * - [BottomBarState.Capsule]: while scrolling a website — the bar shrinks
 *   to a draggable capsule showing only the main domain; tapping expands
 *   it back to the function bar.
 * The regular bar slides down out of the way when a website opens.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverOmnibox(
    tab: BrowserTab,
    showingPage: Boolean,
    collapsed: Boolean,
    reduceMotion: Boolean,
    tabCount: Int,
    glass: Modifier,
    solidStroke: Boolean,
    entries: List<NavEntry>,
    current: Dest,
    onSelectDest: (Dest) -> Unit,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onTabs: () -> Unit,
    onMenuClick: () -> Unit,
    onEditing: (Boolean) -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember(tab.id) { mutableStateOf(false) }
    var draft by remember(tab.id) { mutableStateOf("") }
    val loading = tab.progress in 1..99
    val domain = mainDomain(tab.url)
    val motion = MaterialTheme.motionScheme

    // Capsule dock state lives above AnimatedContent so a drag offset survives
    // the expand/collapse transitions. The dock persists across recreation.
    var dockName by rememberSaveable { mutableStateOf(CapsuleDock.Center.name) }
    val dock = runCatching { CapsuleDock.valueOf(dockName) }.getOrDefault(CapsuleDock.Center)
    val capsuleOffset = remember { Animatable(0f) }
    val dragScope = rememberCoroutineScope()

    val state = bottomBarState(showingPage, collapsed, editing)

    Column(modifier.fillMaxWidth()) {
        if (loading && showingPage) {
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
                if ((initialState == BottomBarState.Nav) != (targetState == BottomBarState.Nav)) {
                    (slideInVertically(motion.defaultSpatialSpec()) { it } +
                        fadeIn(motion.defaultEffectsSpec())) togetherWith
                        (slideOutVertically(motion.defaultSpatialSpec()) { it } +
                            fadeOut(motion.defaultEffectsSpec()))
                } else {
                    fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.defaultEffectsSpec())
                }
            },
            label = "bottomBar",
        ) { currentState ->
            when (currentState) {
                BottomBarState.Nav -> {
                    DiscoverFloatingNavBar(
                        entries = entries,
                        current = current,
                        onSelect = onSelectDest,
                        glass = glass,
                        solidStroke = solidStroke,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                BottomBarState.FunctionBar -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clip(CircleShape)
                            .then(glass)
                            .then(
                                if (solidStroke) {
                                    Modifier.border(TokiChromeStroke.width, MaterialTheme.colorScheme.primary, CircleShape)
                                } else {
                                    Modifier
                                },
                            )
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LongPressLabel("后退") {
                            IconButton(onClick = onBack, enabled = tab.canGoBack, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "后退")
                            }
                        }
                        LongPressLabel("前进") {
                            IconButton(onClick = onForward, enabled = tab.canGoForward, modifier = Modifier.size(48.dp)) {
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
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .clickable {
                                        editing = true
                                        draft = if (tab.url == UrlNormalizer.NEW_TAB_URL) "" else tab.url
                                        onEditing(true)
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Text(
                                    text = (if (tab.isPrivate) "无痕 · " else "") +
                                        if (tab.url == UrlNormalizer.NEW_TAB_URL) "搜索或输入网址" else tab.url,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        LongPressLabel(if (loading) "停止" else "刷新") {
                            IconButton(onClick = onReloadOrStop, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    if (loading) Icons.Filled.Close else Icons.Filled.Refresh,
                                    contentDescription = if (loading) "停止" else "刷新",
                                )
                            }
                        }
                        LongPressLabel("页面菜单") {
                            IconButton(onClick = onMenuClick, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "页面菜单")
                            }
                        }
                        TabCountButton(tabCount, onTabs)
                    }
                }
                BottomBarState.Capsule -> {
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
                                    .height(56.dp)
                                    .clip(CircleShape)
                                    .then(glass)
                                    .then(
                                        if (solidStroke) {
                                            Modifier.border(TokiChromeStroke.width, MaterialTheme.colorScheme.primary, CircleShape)
                                        } else {
                                            Modifier
                                        },
                                    )
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
                                    text = (if (tab.isPrivate) "无痕 · " else "") +
                                        domain.ifBlank { tab.title.ifBlank { "新标签页" } },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 16.dp),
                                )
                            }
                        }
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
