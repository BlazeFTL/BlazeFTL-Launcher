package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppItem
import com.example.model.LauncherSettings
import com.example.ui.components.AppIconBadge

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

    val filteredApps = remember(searchQuery, allApps) {
        if (searchQuery.isBlank()) {
            allApps
        } else {
            allApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val overlayAlpha = (settings.drawerBackgroundOpacity / 100f).coerceIn(0.2f, 0.95f)
    val gridState = rememberLazyGridState()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var dragOffsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0) {
                            dragOffsetY += dragAmount
                            if (dragOffsetY > 45f && gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0) {
                                dragOffsetY = 0f
                                onCloseDrawer()
                            }
                        } else {
                            dragOffsetY = 0f
                        }
                    },
                    onDragEnd = { dragOffsetY = 0f }
                )
            }
    ) {
        // Translucent overlay over the system wallpaper (SS 1)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Drag Down Close Handle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
                    .clickable { onCloseDrawer() }
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.55f))
                )
            }

            // Top Search Bar (Matches SS 1)
            if (settings.appSearchBar) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xEEFFFFFF),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            // App Count / Status info Header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${filteredApps.size} apps",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.65f)
                )

                Text(
                    text = "A - Z",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }

            // Grid of all apps (6 columns, scrolls seamlessly behind navigation keys like SS 1)
            val iconScale = (settings.iconSizePercent / 100f).coerceIn(0.6f, 1.4f)
            val iconBaseDp = (48 * iconScale).dp
            val rowHeightMultiplier = (settings.drawerRowHeight / 75f).coerceIn(0.6f, 1.3f)
            val verticalSpacing = (14 * rowHeightMultiplier).dp
            val fontSizeSp = (11.5f * (settings.fontSizePercent / 100f)).sp
            val bottomPadding = navBarBottomPadding + 20.dp

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(6),
                contentPadding = PaddingValues(
                    start = 4.dp,
                    end = 4.dp,
                    top = 8.dp,
                    bottom = bottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
            ) {
                items(
                    items = filteredApps,
                    key = { it.uniqueKey },
                    contentType = { "drawer_app" }
                ) { app ->
                    DrawerAppItemView(
                        app = app,
                        iconSizeDp = iconBaseDp,
                        fontSizeSp = fontSizeSp,
                        showLabel = settings.iconLabelsInDrawer,
                        maxLines = settings.maxLabelLines,
                        forceMonochrome = settings.forceMonochrome && settings.themedIconsInDrawer,
                        onClick = { onAppClick(app) },
                        onLongClick = { selectedAppForPopup = app }
                    )
                }
            }
        }

        // App Item Long Press Popup
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
                            AppIconBadge(app = app, sizeDp = 40.dp)
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
    iconSizeDp: Dp,
    fontSizeSp: TextUnit,
    showLabel: Boolean,
    maxLines: Int,
    forceMonochrome: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 2.dp)
    ) {
        AppIconBadge(
            app = app,
            sizeDp = iconSizeDp,
            forceMonochrome = forceMonochrome
        )
        if (showLabel) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = app.label,
                color = Color.White,
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
