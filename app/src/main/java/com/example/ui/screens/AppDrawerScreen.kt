package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import com.example.util.IconShapeHelper
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppItem
import com.example.model.LauncherSettings
import com.example.ui.components.AppIconBadge
import kotlinx.coroutines.launch

@Composable
fun AppDrawerScreen(
    settings: LauncherSettings,
    allApps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    onAddToHome: (String) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForPopup by remember { mutableStateOf<AppItem?>(null) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val filteredApps = remember(searchQuery, allApps) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            allApps
        } else {
            // Prioritize prefix and word-start matches (e.g., searching "MT" shows only the apps starting with "MT")
            val prefixOrWordMatches = allApps.filter { app ->
                val label = app.label
                label.startsWith(query, ignoreCase = true) ||
                label.split(' ', '-', '_', '.').any { it.startsWith(query, ignoreCase = true) }
            }.sortedWith(
                compareBy(
                    { !it.label.startsWith(query, ignoreCase = true) },
                    { it.label.lowercase() }
                )
            )

            if (prefixOrWordMatches.isNotEmpty()) {
                prefixOrWordMatches
            } else {
                allApps.filter { it.label.contains(query, ignoreCase = true) }
                    .sortedBy { it.label.lowercase() }
            }
        }
    }

    val iconShape = remember(settings.iconShape) { IconShapeHelper.getShape(settings.iconShape) }

    // Background Opacity makes background translucent light scrim over wallpaper
    val overlayAlpha = (settings.drawerBackgroundOpacity / 100f).coerceIn(0f, 1f)
    val isLightBackground = settings.drawerBackgroundOpacity >= 40
    val labelColor = if (isLightBackground) Color(0xFF1E293B) else Color.White
    val headerTextColor = if (isLightBackground) Color(0xFF475569) else Color.White.copy(alpha = 0.85f)
    val handleColor = if (isLightBackground) Color(0xFF94A3B8) else Color.White.copy(alpha = 0.75f)

    val gridState = rememberLazyGridState()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var isGestureStartedAtTop by remember { mutableStateOf(false) }
    var accumulatedPullDown by remember { mutableFloatStateOf(0f) }

    // Nested scroll connection: Only close with pull down if the gesture started while ALREADY at the top
    val pullDownConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If the gesture started while already at top, and user is pulling down
                if (isGestureStartedAtTop && available.y > 0f) {
                    accumulatedPullDown += available.y
                    if (accumulatedPullDown > 22f) {
                        accumulatedPullDown = 0f
                        isGestureStartedAtTop = false
                        onCloseDrawer()
                        return Offset(0f, available.y)
                    }
                } else if (available.y < -5f) {
                    accumulatedPullDown = 0f
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // Only close if gesture started at top. If user was scrolling up from below, it stops at top and NEVER closes.
                if (isGestureStartedAtTop && available.y > 0f) {
                    accumulatedPullDown += available.y
                    if (accumulatedPullDown > 18f) {
                        accumulatedPullDown = 0f
                        isGestureStartedAtTop = false
                        onCloseDrawer()
                        return Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                accumulatedPullDown = 0f
                if (isGestureStartedAtTop && available.y > 60f) {
                    isGestureStartedAtTop = false
                    onCloseDrawer()
                    return Velocity(0f, available.y)
                }
                isGestureStartedAtTop = false
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    // At the exact moment finger touches down: was the grid already resting at top?
                    val atTop = !gridState.canScrollBackward && 
                        gridState.firstVisibleItemIndex == 0 && 
                        gridState.firstVisibleItemScrollOffset == 0
                    isGestureStartedAtTop = atTop
                    accumulatedPullDown = 0f

                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })

                    isGestureStartedAtTop = false
                    accumulatedPullDown = 0f
                }
            }
            .nestedScroll(pullDownConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Drag Down Close Handle & Header Touch Area
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
                    .clickable { onCloseDrawer() }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 8f) {
                                onCloseDrawer()
                            }
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(handleColor)
                )
            }

            // Top Search Bar (Matches SS 1 & Blaze Launcher)
            if (settings.appSearchBar) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFD6DEE6)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF5F6368),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search this phone and more...",
                                    color = Color(0xFF5F6368),
                                    fontSize = 15.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(
                                    color = Color(0xFF202124),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFFE85D54)),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth().testTag("drawer_search_input")
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF5F6368),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onShowToast("Google Lens Vision Search") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Lens",
                                tint = Color(0xFFEA4335),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // App Count / Status info Header (e.g. 153 apps   A - Z)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 8f) {
                                onCloseDrawer()
                            }
                        }
                    }
            ) {
                Text(
                    text = "${filteredApps.size} apps",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = headerTextColor
                )

                Text(
                    text = "A - Z",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = headerTextColor
                )
            }

            // Calculations for Dynamic Sizing & Grid Settings (Allows up to 7x10)
            val gridColumns = settings.drawerGridColumns.coerceIn(3, 7)
            val gridRows = settings.drawerGridRows.coerceIn(4, 10)
            val iconScale = (settings.iconSizePercent / 100f).coerceIn(0.5f, 1.5f)
            val iconBaseDp = (48 * iconScale).dp
            val baseHeight = (settings.drawerRowHeight * 1.12f) * (9f / gridRows)
            val rowHeightDp = baseHeight.coerceIn(50f, 125f).dp
            val verticalSpacing = ((settings.drawerRowHeight - 35) * 0.4f).coerceAtLeast(3f).dp
            val fontSizeSp = (11.5f * (settings.fontSizePercent / 100f)).sp
            val bottomPadding = navBarBottomPadding + 24.dp

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Grid of all apps (Custom columns up to 7, scrolls seamlessly behind navigation keys)
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(gridColumns),
                    contentPadding = PaddingValues(
                        start = 4.dp,
                        end = 12.dp, // Leave breathing room for scrollbar
                        top = 6.dp,
                        bottom = bottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredApps,
                        key = { it.uniqueKey },
                        contentType = { "drawer_app" }
                    ) { app ->
                        DrawerAppItemView(
                            app = app,
                            rowHeightDp = rowHeightDp,
                            iconSizeDp = iconBaseDp,
                            fontSizeSp = fontSizeSp,
                            showLabel = settings.iconLabelsInDrawer,
                            maxLines = settings.maxLabelLines,
                            labelColor = labelColor,
                            iconShape = iconShape,
                            forceMonochrome = settings.forceMonochrome && settings.themedIconsInDrawer,
                            showNotificationDot = settings.notificationDots,
                            onClick = { onAppClick(app) },
                            onLongClick = { selectedAppForPopup = app }
                        )
                    }
                }

                // Smooth Fast-Scroll Scrollbar on right edge
                DrawerScrollBar(
                    totalItems = filteredApps.size,
                    firstVisibleIndex = { gridState.firstVisibleItemIndex },
                    isLightBackground = isLightBackground,
                    currentLetter = { filteredApps.getOrNull(gridState.firstVisibleItemIndex)?.label?.take(1)?.uppercase() ?: "A" },
                    onScrub = { targetIndex ->
                        coroutineScope.launch {
                            gridState.scrollToItem(targetIndex)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(end = 2.dp, bottom = bottomPadding)
                )
            }
        }

        // App Item Long Press Popup Menu
        if (selectedAppForPopup != null) {
            val app = selectedAppForPopup!!
            Dialog(onDismissRequest = { selectedAppForPopup = null }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.88f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppIconBadge(app = app, sizeDp = 42.dp, shape = iconShape)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = app.label,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF231F20)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedAppForPopup = null
                                    onAppClick(app)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.GridView, contentDescription = null, tint = Color(0xFF382F2D), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "Open App", fontSize = 16.sp, color = Color(0xFF231F20))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedAppForPopup = null
                                    onAddToHome(app.packageName)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF382F2D), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "Add to Home Screen", fontSize = 16.sp, color = Color(0xFF231F20))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedAppForPopup = null
                                    onOpenAppInfo(app.packageName)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFF382F2D), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "App info", fontSize = 16.sp, color = Color(0xFF231F20))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedAppForPopup = null
                                    onUninstallApp(app.packageName)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFF382F2D), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "Uninstall", fontSize = 16.sp, color = Color(0xFF231F20))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerAppItemView(
    app: AppItem,
    rowHeightDp: Dp,
    iconSizeDp: Dp,
    fontSizeSp: TextUnit,
    showLabel: Boolean,
    maxLines: Int,
    labelColor: Color,
    iconShape: Shape = CircleShape,
    forceMonochrome: Boolean,
    showNotificationDot: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(rowHeightDp)
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // Eliminates heavy ripple allocation during fast scrolling
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        AppIconBadge(
            app = app,
            sizeDp = iconSizeDp,
            shape = iconShape,
            forceMonochrome = forceMonochrome,
            showNotificationDot = showNotificationDot
        )
        if (showLabel) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = app.label,
                color = labelColor,
                fontSize = fontSizeSp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
fun DrawerScrollBar(
    totalItems: Int,
    firstVisibleIndex: () -> Int,
    isLightBackground: Boolean,
    currentLetter: () -> String,
    onScrub: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalItems <= 12) return

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val scrollFraction by remember(totalItems) {
        derivedStateOf {
            val idx = firstVisibleIndex()
            if (totalItems > 1) {
                (idx.toFloat() / (totalItems - 1).toFloat()).coerceIn(0f, 1f)
            } else 0f
        }
    }

    val activeFraction = if (isDragging) dragProgress else scrollFraction
    val trackColor = if (isLightBackground) Color(0x20000000) else Color(0x30FFFFFF)
    val thumbColor = if (isLightBackground) Color(0xFF64748B) else Color.White.copy(alpha = 0.85f)

    BoxWithConstraints(
        modifier = modifier
            .width(28.dp)
            .pointerInput(totalItems) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val fraction = (offset.y / size.height).coerceIn(0f, 1f)
                        dragProgress = fraction
                        val target = (fraction * (totalItems - 1)).toInt().coerceIn(0, totalItems - 1)
                        onScrub(target)
                    },
                    onVerticalDrag = { change, _ ->
                        val fraction = (change.position.y / size.height).coerceIn(0f, 1f)
                        dragProgress = fraction
                        val target = (fraction * (totalItems - 1)).toInt().coerceIn(0, totalItems - 1)
                        onScrub(target)
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false }
                )
            }
    ) {
        val totalHeight = maxHeight
        val thumbHeight = 42.dp
        val availableTravel = totalHeight - thumbHeight
        val thumbOffset = availableTravel * activeFraction

        // Vertical Track line
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(trackColor)
        )

        // Draggable Thumb Indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = thumbOffset)
                .width(4.dp)
                .height(thumbHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(thumbColor)
        )

        // Alphabet Indicator Bubble while dragging
        AnimatedVisibility(
            visible = isDragging,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-34).dp, y = (thumbOffset - 8.dp).coerceAtLeast(0.dp))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE85D54))
            ) {
                Text(
                    text = currentLetter(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
