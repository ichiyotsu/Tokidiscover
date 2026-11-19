package com.tokidiscover.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tokidiscover.app.data.BrowserTab
import com.tokidiscover.app.data.FunctionBarComponent
import com.tokidiscover.app.data.UrlNormalizer
import com.tokidiscover.app.data.label
import com.tokidiscover.app.ui.BottomBarState
import com.tokidiscover.app.ui.Dest
import com.tokidiscover.app.ui.bottomBarState
import com.tokidiscover.app.ui.browser.SettingsPage
import com.tokidiscover.app.ui.browser.label
import com.tokidiscover.app.ui.theme.TokiChromeStroke
import com.tokidiscover.app.ui.theme.TokiScrollChipBackdrop
import com.tokidiscover.app.ui.theme.TokiSpacing
import dev.chrisbanes.haze.HazeState
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
 * Jelly deformation for a long-press drag: the pill stretches along the
 * drag direction and squashes across it, with a slight rotation — like
 * jelly. Pure math, so it is unit-testable (see JellyDragTest).
 */
data class JellyDeformation(val scaleX: Float, val scaleY: Float, val rotationDegrees: Float)

fun jellyDeformation(offsetX: Float, offsetY: Float): JellyDeformation {
    // Stretch follows the drag magnitude on each axis (either direction
    // stretches); only the tilt keeps the drag direction.
    val stretchX = kotlin.math.abs(offsetX / 240f).coerceIn(0f, 1f)
    val stretchY = kotlin.math.abs(offsetY / 240f).coerceIn(0f, 1f)
    return JellyDeformation(
        scaleX = 1f + 0.08f * stretchX - 0.03f * stretchY,
        scaleY = 1f + 0.08f * stretchY - 0.03f * stretchX,
        rotationDegrees = 4f * (offsetX / 240f).coerceIn(-1f, 1f),
    )
}

/**
 * Long-press to drag with a jelly feel. The element follows the finger,
 * stretches along the drag direction, and springs back to its place on
 * release with an overshoot (spring stiffness ~300, dampingRatio ~0.6).
 * Taps still work: only a long-press starts the drag. [reduceMotion]
 * skips the spring and snaps back instead.
 */
@Composable
private fun Modifier.jellyDraggable(reduceMotion: Boolean): Modifier {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    return this
        .graphicsLayer {
            translationX = offsetX.value
            translationY = offsetY.value
            val deformation = jellyDeformation(offsetX.value, offsetY.value)
            scaleX = deformation.scaleX
            scaleY = deformation.scaleY
            rotationZ = deformation.rotationDegrees
        }
        .pointerInput(reduceMotion) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDrag = { _, dragAmount ->
                    scope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount.x)
                        offsetY.snapTo(offsetY.value + dragAmount.y)
                    }
                },
                onDragEnd = {
                    scope.launch {
                        if (reduceMotion) {
                            offsetX.snapTo(0f)
                            offsetY.snapTo(0f)
                        } else {
                            offsetX.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.6f))
                            offsetY.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.6f))
                        }
                    }
                },
                onDragCancel = {
                    scope.launch {
                        if (reduceMotion) {
                            offsetX.snapTo(0f)
                            offsetY.snapTo(0f)
                        } else {
                            offsetX.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.6f))
                            offsetY.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.6f))
                        }
                    }
                },
            )
        }
}

/**
 * The one title header for the whole app (TokiStudio 0.1.10): a single text
 * role (headlineMedium, Bold), a 56dp round back chip, a fixed 12dp gap.
 * The frost is always on — it never fades in or out with scroll.
 * Long-press the frosted title pill to drag it with a jelly wobble; it
 * always springs back to its place, so the drag never moves anything
 * permanently and never breaks the back chip or a title tap. When
 * [onTitleClick] is set, tapping the pill runs it and a dropdown arrow
 * is shown (the Settings page uses this for its page switcher).
 */
@Composable
fun OverlayTitleHeader(
    title: String,
    onBack: (() -> Unit)?,
    onHeightMeasured: (Dp) -> Unit,
    backdropBlur: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    onTitleClick: (() -> Unit)? = null,
    reduceMotion: Boolean = false,
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
                .clip(titleShape)
                .then(if (onTitleClick != null) Modifier.clickable { onTitleClick() } else Modifier)
                .jellyDraggable(reduceMotion),
            contentAlignment = Alignment.CenterStart,
        ) {
            TokiScrollChipBackdrop(
                shape = titleShape,
                backdropBlur = backdropBlur,
                hazeState = hazeState,
                modifier = Modifier.matchParentSize(),
            )
            Row(
                Modifier.padding(horizontal = TokiSpacing.titleGlyphInset),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (onTitleClick != null) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "切换设置页",
                        tint = content,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
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

/**
 * The shared dropdown panel (TokiUI 0.1.11 glass, 28dp corners): the ⋮
 * menu and the Settings title switcher are the same component, so their
 * look and feel stay identical.
 */
@Composable
fun TokiMenuPanel(
    glass: Modifier,
    solidStroke: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
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
        content = content,
    )
}

@Composable
private fun MenuActionRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    selected: Boolean = false,
) {
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
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "当前页",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Icon for a settings page, for the title switcher dropdown. */
private fun settingsPageIcon(page: SettingsPage): ImageVector = when (page) {
    SettingsPage.Main -> Icons.Filled.Settings
    SettingsPage.Appearance -> Icons.Filled.Palette
    SettingsPage.SearchEngine -> Icons.Filled.Search
    SettingsPage.Browsing -> Icons.Filled.Public
    SettingsPage.ClearData -> Icons.Filled.DeleteSweep
    SettingsPage.Update -> Icons.Filled.SystemUpdate
    SettingsPage.About -> Icons.Outlined.Info
    SettingsPage.FunctionBar -> Icons.Filled.Tune
}

/**
 * The Settings title switcher: same panel as the ⋮ menu, listing every
 * settings page so the user can jump between them without going back.
 */
@Composable
fun SettingsPageDropdown(
    current: SettingsPage,
    onSelect: (SettingsPage) -> Unit,
    glass: Modifier,
    solidStroke: Boolean,
    modifier: Modifier = Modifier,
) {
    TokiMenuPanel(glass, solidStroke, modifier) {
        SettingsPage.entries.forEach { page ->
            MenuActionRow(
                label = page.label(),
                icon = settingsPageIcon(page),
                onClick = { onSelect(page) },
                selected = page == current,
            )
        }
    }
}

/** Static icon for a function bar component (the bar itself overrides bookmark/refresh by state). */
fun functionBarComponentIcon(component: FunctionBarComponent): ImageVector = when (component) {
    FunctionBarComponent.Search -> Icons.Filled.Search
    FunctionBarComponent.Back -> Icons.AutoMirrored.Filled.ArrowBack
    FunctionBarComponent.Forward -> Icons.AutoMirrored.Filled.ArrowForward
    FunctionBarComponent.Refresh -> Icons.Filled.Refresh
    FunctionBarComponent.Tabs -> Icons.Outlined.Tab
    FunctionBarComponent.Bookmark -> Icons.Outlined.BookmarkBorder
    FunctionBarComponent.Share -> Icons.Outlined.Share
    FunctionBarComponent.Find -> Icons.Outlined.FindInPage
    FunctionBarComponent.Home -> Icons.Filled.Home
    FunctionBarComponent.Settings -> Icons.Filled.Settings
}

/**
 * The "更多" menu, structured like Chrome's overflow menu (Simplified
 * Chinese, TokiUI 0.1.10 glass): new tab / private tab / add to group,
 * history / clear data / downloads / bookmarks / recently closed / tabs,
 * settings / customize new tab / about — plus the page actions
 * (bookmark / find / share / translate) in a final section. The app's
 * destination nav lives in the bottom Nav bar on the home page; on a
 * website this menu is the only way to reach 标签页 / 书库 / 设置, so
 * those entries are included here.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MoreMenuPanel(
    bookmarked: Boolean,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onFind: () -> Unit,
    onTranslate: () -> Unit,
    onNewTab: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onAddToGroup: () -> Unit,
    onHistory: () -> Unit,
    onClearData: () -> Unit,
    onDownloads: () -> Unit,
    onBookmarks: () -> Unit,
    onRecentTabs: () -> Unit,
    onTabs: () -> Unit,
    onSettings: () -> Unit,
    onCustomizeNewTab: () -> Unit,
    onAbout: () -> Unit,
    glass: Modifier,
    solidStroke: Boolean,
    modifier: Modifier = Modifier,
) {
    TokiMenuPanel(glass, solidStroke, modifier) {
        MenuActionRow("新标签页", Icons.Filled.Add, onNewTab)
        MenuActionRow("新无痕标签页", Icons.Filled.VisibilityOff, onNewPrivateTab)
        MenuActionRow("将标签页加入新群组", Icons.Filled.GroupAdd, onAddToGroup)
        HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        MenuActionRow("历史记录", Icons.Filled.History, onHistory)
        MenuActionRow("删除浏览数据", Icons.Filled.DeleteSweep, onClearData)
        MenuActionRow("下载", Icons.Filled.Download, onDownloads)
        MenuActionRow("书签", Icons.Outlined.BookmarkBorder, onBookmarks)
        MenuActionRow("最近打开的标签页", Icons.Filled.Restore, onRecentTabs)
        MenuActionRow("标签页", Icons.Outlined.Tab, onTabs)
        HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        MenuActionRow("设置", Icons.Filled.Settings, onSettings)
        MenuActionRow("自定义新标签页", Icons.Filled.Edit, onCustomizeNewTab)
        MenuActionRow("说明和意见反馈", Icons.Outlined.Info, onAbout)
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

/**
 * The bottom bar in its two shapes (TokiUI 0.1.11):
 * - [BottomBarState.Nav]: the regular floating glass capsule nav, on the
 *   home page and the app destinations.
 * - [BottomBarState.FunctionBar]: on a website — outer glass capsule,
 *   inner pill with the full URL, and the configured page actions around
 *   it. The bar never collapses and never hides: it stays put.
 * The function bar is configurable (see FunctionBarConfig): each button
 * is one component, at most seven including the search pill; long-press
 * a button and drag it sideways to reorder.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverOmnibox(
    tab: BrowserTab,
    showingPage: Boolean,
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
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onFind: () -> Unit,
    onHome: () -> Unit,
    onMenuClick: () -> Unit,
    bookmarked: Boolean,
    barItems: List<FunctionBarComponent>,
    onMoveBarItem: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember(tab.id) { mutableStateOf(false) }
    var draft by remember(tab.id) { mutableStateOf("") }
    val loading = tab.progress in 1..99
    val state = bottomBarState(showingPage)
    val motion = MaterialTheme.motionScheme

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
                    FunctionBar(
                        tab = tab,
                        editing = editing,
                        draft = draft,
                        onDraftChange = { draft = it },
                        onEditRequest = {
                            editing = true
                            draft = if (tab.url == UrlNormalizer.NEW_TAB_URL) "" else tab.url
                        },
                        onSubmit = {
                            onSubmit(it)
                            editing = false
                        },
                        loading = loading,
                        bookmarked = bookmarked,
                        tabCount = tabCount,
                        barItems = barItems,
                        onMoveBarItem = onMoveBarItem,
                        onBack = onBack,
                        onForward = onForward,
                        onReloadOrStop = onReloadOrStop,
                        onTabs = onTabs,
                        onBookmark = onBookmark,
                        onShare = onShare,
                        onFind = onFind,
                        onHome = onHome,
                        onMenuClick = onMenuClick,
                        glass = glass,
                        solidStroke = solidStroke,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/**
 * The configurable function bar: one 72dp glass capsule holding the
 * configured components in order. The search pill takes the remaining
 * width (min 48dp so it stays tappable); every other component is a
 * 48dp button. Long-press a component and drag it sideways to reorder —
 * the order is saved as you drag. Dragging is disabled while the
 * address field is being edited.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FunctionBar(
    tab: BrowserTab,
    editing: Boolean,
    draft: String,
    onDraftChange: (String) -> Unit,
    onEditRequest: () -> Unit,
    onSubmit: (String) -> Unit,
    loading: Boolean,
    bookmarked: Boolean,
    tabCount: Int,
    barItems: List<FunctionBarComponent>,
    onMoveBarItem: (Int, Int) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onTabs: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onFind: () -> Unit,
    onHome: () -> Unit,
    onMenuClick: () -> Unit,
    glass: Modifier,
    solidStroke: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val dragScope = rememberCoroutineScope()
    // The component being dragged, tracked by identity (not index) so the
    // drag survives the list reordering under it.
    var draggingComponent by remember { mutableStateOf<FunctionBarComponent?>(null) }
    var dragAccumPx by remember { mutableFloatStateOf(0f) }
    val dragOffset = remember { Animatable(0f) }
    val currentItems by rememberUpdatedState(barItems)
    val currentOnMove by rememberUpdatedState(onMoveBarItem)
    // One reorder step is one button wide; the wide search pill just needs
    // a longer drag to cross it.
    val stepPx = with(density) { 56.dp.toPx() }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(editing) {
        if (editing) focusRequester.requestFocus()
    }

    /** Long-press drag for one bar item: sideways reorder, spring back on release. */
    fun Modifier.barDrag(component: FunctionBarComponent): Modifier = pointerInput(component) {
        detectDragGesturesAfterLongPress(
            onDragStart = {
                draggingComponent = component
                dragAccumPx = 0f
                dragScope.launch { dragOffset.snapTo(0f) }
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onDrag = { _, dragAmount ->
                val items = currentItems
                val from = items.indexOf(component)
                if (from < 0) return@detectDragGesturesAfterLongPress
                dragAccumPx += dragAmount.x
                dragScope.launch { dragOffset.snapTo(dragAccumPx) }
                val steps = (dragAccumPx / stepPx).toInt()
                if (steps != 0) {
                    val to = (from + steps).coerceIn(0, items.lastIndex)
                    if (to != from) currentOnMove(from, to)
                    dragAccumPx -= steps * stepPx
                }
            },
            onDragEnd = {
                dragAccumPx = 0f
                dragScope.launch {
                    dragOffset.animateTo(0f, spring(stiffness = 400f, dampingRatio = 0.7f))
                    draggingComponent = null
                }
            },
            onDragCancel = {
                dragAccumPx = 0f
                dragScope.launch {
                    dragOffset.animateTo(0f, spring(stiffness = 400f, dampingRatio = 0.7f))
                    draggingComponent = null
                }
            },
        )
    }

    /** One draggable bar item: follows the finger while dragged, pops up a little. */
    @Composable
    fun BarItemWrapper(component: FunctionBarComponent, content: @Composable () -> Unit) {
        val isDragging = draggingComponent == component
        val scale by animateFloatAsState(
            targetValue = if (isDragging) 1.08f else 1f,
            animationSpec = spring(stiffness = 400f, dampingRatio = 0.7f),
            label = "barDragScale",
        )
        Box(
            Modifier
                .zIndex(if (isDragging) 1f else 0f)
                .graphicsLayer {
                    translationX = if (isDragging) dragOffset.value else 0f
                    scaleX = scale
                    scaleY = scale
                }
                .then(if (!editing) Modifier.barDrag(component) else Modifier),
            contentAlignment = Alignment.Center,
        ) { content() }
    }

    @Composable
    fun BarButton(
        component: FunctionBarComponent,
        label: String,
        icon: ImageVector,
        onClick: () -> Unit,
        enabled: Boolean = true,
    ) {
        BarItemWrapper(component) {
            LongPressLabel(label) {
                IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(48.dp)) {
                    Icon(icon, contentDescription = label)
                }
            }
        }
    }

    Row(
        modifier
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
        barItems.forEach { component ->
            key(component) {
                when (component) {
                    FunctionBarComponent.Search -> {
                        val isDragging = draggingComponent == FunctionBarComponent.Search
                        val scale by animateFloatAsState(
                            targetValue = if (isDragging) 1.04f else 1f,
                            animationSpec = spring(stiffness = 400f, dampingRatio = 0.7f),
                            label = "barDragScale",
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .widthIn(min = 48.dp)
                                .height(56.dp)
                                .zIndex(if (isDragging) 1f else 0f)
                                .graphicsLayer {
                                    translationX = if (isDragging) dragOffset.value else 0f
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .then(if (!editing) Modifier.barDrag(FunctionBarComponent.Search) else Modifier)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .then(if (!editing) Modifier.clickable { onEditRequest() } else Modifier)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (editing) {
                                androidx.compose.material3.TextField(
                                    value = draft,
                                    onValueChange = onDraftChange,
                                    singleLine = true,
                                    placeholder = { Text("搜索或输入网址") },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                    keyboardActions = KeyboardActions(onGo = { onSubmit(draft) }),
                                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    ),
                                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                )
                            } else {
                                Text(
                                    text = (if (tab.isPrivate) "无痕 · " else "") +
                                        if (tab.url == UrlNormalizer.NEW_TAB_URL) "搜索或输入网址" else tab.url,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    FunctionBarComponent.Back -> BarButton(
                        component, "后退", Icons.AutoMirrored.Filled.ArrowBack, onBack,
                        enabled = tab.canGoBack,
                    )
                    FunctionBarComponent.Forward -> BarButton(
                        component, "前进", Icons.AutoMirrored.Filled.ArrowForward, onForward,
                        enabled = tab.canGoForward,
                    )
                    FunctionBarComponent.Refresh -> BarButton(
                        component,
                        if (loading) "停止" else "刷新",
                        if (loading) Icons.Filled.Close else Icons.Filled.Refresh,
                        onReloadOrStop,
                    )
                    FunctionBarComponent.Tabs -> BarItemWrapper(component) {
                        TabCountButton(tabCount, onTabs)
                    }
                    FunctionBarComponent.Bookmark -> BarButton(
                        component,
                        if (bookmarked) "已收藏" else "收藏",
                        if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        onBookmark,
                    )
                    FunctionBarComponent.Share -> BarButton(
                        component, "分享", Icons.Outlined.Share, onShare,
                    )
                    FunctionBarComponent.Find -> BarButton(
                        component, "页内查找", Icons.Outlined.FindInPage, onFind,
                    )
                    FunctionBarComponent.Home -> BarButton(
                        component, "主页", Icons.Filled.Home, onHome,
                    )
                    FunctionBarComponent.Settings -> BarButton(
                        component, "页面菜单", Icons.Filled.MoreVert, onMenuClick,
                    )
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
