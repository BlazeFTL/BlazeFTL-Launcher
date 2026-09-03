package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.AppItem
import com.example.model.LauncherScreen
import com.example.model.LauncherSettings
import com.example.ui.components.AppIconBadge

import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset

@Composable
fun HomeScreen(
    settings: LauncherSettings,
    homeApps: List<AppItem>,
    dockApps: List<AppItem>,
    isMusicPlaying: Boolean = false,
    onAppClick: (AppItem) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenRecents: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWallpaper: () -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onRemoveFromHome: (String) -> Unit,
    onExpandQuickSettings: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDesktopMenu by remember { mutableStateOf(false) }
    var selectedAppForPopup by remember { mutableStateOf<AppItem?>(null) }
    var currentSongIndex by remember { mutableIntStateOf(0) }

    val songList = listOf(
        "Базовый минимум (Slow Version)" to "Sabi - Topic",
        "Starboy (Aesthetic Remix)" to "The Weeknd",
        "Midnight City" to "M83",
        "Nightcall (Synthwave)" to "Kavinsky"
    )

    var totalDragY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .draggable(
                state = rememberDraggableState { delta ->
                    totalDragY += delta
                    if (totalDragY < -30f) {
                        totalDragY = 0f
                        onOpenDrawer()
                    } else if (totalDragY > 50f) {
                        totalDragY = 0f
                        onExpandQuickSettings()
                    }
                },
                orientation = Orientation.Vertical,
                onDragStopped = { totalDragY = 0f }
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        showDesktopMenu = true
                    },
                    onDoubleTap = {
                        if (settings.doubleTapToSleep) {
                            onShowToast("Double tap: Screen locked")
                        }
                    }
                )
            }
    ) {
        // Status bar shadow overlay if enabled
        if (settings.statusBarShadow) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Main Desktop Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // QuickSpace / At A Glance Widget
            if (settings.atAGlance) {
                QuickspaceWidget(
                    settings = settings,
                    isMusicPlaying = isMusicPlaying,
                    currentSong = songList[currentSongIndex],
                    onSongClick = {
                        if (isMusicPlaying) {
                            currentSongIndex = (currentSongIndex + 1) % songList.size
                            onShowToast("Track: ${songList[currentSongIndex].first}")
                        } else {
                            onShowToast("Calendar & At-A-Glance")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Desktop App Grid (Responsive 6 columns)
            val iconScale = (settings.iconSizePercent / 100f).coerceIn(0.6f, 1.4f)
            val iconBaseDp = (48 * iconScale).dp
            val fontSizeSp = (11.5f * (settings.fontSizePercent / 100f)).sp

            if (homeApps.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    items(homeApps, key = { it.uniqueKey }, contentType = { "desktop_app" }) { app ->
                        DesktopAppIcon(
                            app = app,
                            iconSizeDp = iconBaseDp,
                            fontSizeSp = fontSizeSp,
                            showLabel = settings.iconLabelsOnDesktop,
                            maxLines = settings.maxLabelLines,
                            forceMonochrome = settings.forceMonochrome && settings.themedIcons,
                            showNotificationDot = settings.notificationDots,
                            onClick = { onAppClick(app) },
                            onLongClick = {
                                if (settings.lockLayout) {
                                    onShowToast("Desktop layout is locked in settings")
                                } else {
                                    selectedAppForPopup = app
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Hotseat Dock (Apps placed ABOVE bottom search bar)
            HotseatDock(
                settings = settings,
                dockApps = dockApps,
                iconSizeDp = iconBaseDp,
                forceMonochrome = settings.forceMonochrome && settings.themedIcons,
                onAppClick = onAppClick,
                onOpenDrawer = onOpenDrawer,
                onOpenRecents = onOpenRecents,
                modifier = Modifier.fillMaxWidth()
            )

            // Dock Search Bar at the VERY BOTTOM below dock apps
            if (settings.googleSearchBar) {
                DockSearchBar(
                    settings = settings,
                    onSearchClick = onOpenDrawer,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }

        // Desktop Long Press Dialog
        if (showDesktopMenu) {
            DesktopContextMenu(
                onDismiss = { showDesktopMenu = false },
                onOpenSettings = {
                    showDesktopMenu = false
                    onOpenSettings()
                },
                onOpenWallpaper = {
                    showDesktopMenu = false
                    onOpenWallpaper()
                },
                onShowToast = onShowToast
            )
        }

        // App Item Long Press Popup
        if (selectedAppForPopup != null) {
            val app = selectedAppForPopup!!
            AppItemContextMenu(
                app = app,
                onDismiss = { selectedAppForPopup = null },
                onOpenApp = {
                    selectedAppForPopup = null
                    onAppClick(app)
                },
                onOpenAppInfo = {
                    selectedAppForPopup = null
                    onOpenAppInfo(app.packageName)
                },
                onUninstallApp = {
                    selectedAppForPopup = null
                    onUninstallApp(app.packageName)
                },
                onRemoveFromHome = {
                    selectedAppForPopup = null
                    onRemoveFromHome(app.packageName)
                },
                onShowToast = onShowToast
            )
        }
    }
}

@Composable
fun QuickspaceWidget(
    settings: LauncherSettings,
    isMusicPlaying: Boolean,
    currentSong: Pair<String, String>,
    onSongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayOfWeekFormat = remember { SimpleDateFormat("EEEE", Locale.getDefault()) }
    val monthDayFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val hourMinuteFormat = remember { SimpleDateFormat("h:mm", Locale.getDefault()) }
    val amPmFormat = remember { SimpleDateFormat("a", Locale.getDefault()) }

    var currentDayOfWeek by remember { mutableStateOf(dayOfWeekFormat.format(Date())) }
    var currentMonthDay by remember { mutableStateOf(monthDayFormat.format(Date())) }
    var currentHourMinute by remember { mutableStateOf(hourMinuteFormat.format(Date())) }
    var currentAmPm by remember { mutableStateOf(amPmFormat.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentDayOfWeek = dayOfWeekFormat.format(now)
            currentMonthDay = monthDayFormat.format(now)
            currentHourMinute = hourMinuteFormat.format(now)
            currentAmPm = amPmFormat.format(now)
            delay(1000L)
        }
    }

    val widgetShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.45f),
            offset = Offset(0f, 2f),
            blurRadius = 8f
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onSongClick() }
            .padding(vertical = 4.dp)
    ) {
        if (settings.nowPlaying && isMusicPlaying) {
            // Sleek Modern Now Playing Pill
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFE85D54).copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Now Playing",
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong.first,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = androidx.compose.ui.text.TextStyle(shadow = widgetShadow)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "by ${currentSong.second}",
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = androidx.compose.ui.text.TextStyle(shadow = widgetShadow)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Animated Equalizer Visualizer Bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(16.dp)
                    ) {
                        Box(modifier = Modifier.width(3.dp).height(10.dp).background(Color(0xFFFF8A80), RoundedCornerShape(2.dp)))
                        Box(modifier = Modifier.width(3.dp).height(16.dp).background(Color(0xFFFF8A80), RoundedCornerShape(2.dp)))
                        Box(modifier = Modifier.width(3.dp).height(7.dp).background(Color(0xFFFF8A80), RoundedCornerShape(2.dp)))
                    }
                }
            }
        } else {
            // Elegant Pixel-Style At-A-Glance Layout
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Line 1: Date & Weather (Authentic Pixel At-A-Glance Header)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = "$currentDayOfWeek, $currentMonthDay",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.2).sp,
                        style = androidx.compose.ui.text.TextStyle(shadow = widgetShadow)
                    )

                    if (settings.weatherCondition) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            style = androidx.compose.ui.text.TextStyle(shadow = widgetShadow)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Outlined.WbSunny,
                            contentDescription = "Weather",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (settings.detailedWeather) {
                                if (settings.currentCity) "24°C Sunny, New York" else "24°C Sunny"
                            } else {
                                if (settings.currentCity) "24°C, New York" else "24°C"
                            },
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            style = androidx.compose.ui.text.TextStyle(shadow = widgetShadow)
                        )
                    }
                }

                // Line 2: Subtle Time & Status Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Clock",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "$currentHourMinute $currentAmPm",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        style = androidx.compose.ui.text.TextStyle(shadow = widgetShadow)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopAppIcon(
    app: AppItem,
    iconSizeDp: androidx.compose.ui.unit.Dp,
    fontSizeSp: androidx.compose.ui.unit.TextUnit,
    showLabel: Boolean,
    maxLines: Int,
    forceMonochrome: Boolean,
    showNotificationDot: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp)
    ) {
        AppIconBadge(
            app = app,
            sizeDp = iconSizeDp,
            forceMonochrome = forceMonochrome,
            showNotificationDot = showNotificationDot
        )
        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
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

@Composable
fun HotseatDock(
    settings: LauncherSettings,
    dockApps: List<AppItem>,
    iconSizeDp: androidx.compose.ui.unit.Dp,
    forceMonochrome: Boolean,
    onAppClick: (AppItem) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenRecents: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dockBg = if (settings.hotseatBackground) {
        Color.Black.copy(alpha = settings.hotseatBackgroundOpacity / 100f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .background(dockBg, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            dockApps.forEach { app ->
                DesktopAppIcon(
                    app = app,
                    iconSizeDp = iconSizeDp,
                    fontSizeSp = 10.sp,
                    showLabel = false,
                    maxLines = 1,
                    forceMonochrome = forceMonochrome,
                    showNotificationDot = settings.notificationDots,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppClick(app) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DockSearchBar(
    settings: LauncherSettings,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = (settings.searchBarCornerRadius * 0.28f).dp

    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = Color(0xCCFFFFFF),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onSearchClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = Color(0xFF5F6368),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search this phone and more...",
                color = Color(0xFF5F6368),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (settings.musicSearch) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice search",
                    tint = Color(0xFF4285F4),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Lens",
                tint = Color(0xFFEA4335),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun DesktopContextMenu(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWallpaper: () -> Unit,
    onShowToast: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Desktop Options",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF231F20),
                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                )

                ContextMenuItem(
                    icon = Icons.Default.Settings,
                    label = "Home settings",
                    onClick = onOpenSettings,
                    testTag = "menu_home_settings"
                )
                ContextMenuItem(
                    icon = Icons.Default.Wallpaper,
                    label = "Wallpaper & style",
                    onClick = onOpenWallpaper
                )
                ContextMenuItem(
                    icon = Icons.Default.Widgets,
                    label = "Widgets",
                    onClick = {
                        onDismiss()
                        onShowToast("Widgets catalog")
                    }
                )
            }
        }
    }
}

@Composable
fun AppItemContextMenu(
    app: AppItem,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstallApp: () -> Unit,
    onRemoveFromHome: () -> Unit,
    onShowToast: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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

                ContextMenuItem(
                    icon = Icons.Default.GridView,
                    label = "Open App",
                    onClick = onOpenApp
                )
                ContextMenuItem(
                    icon = Icons.Default.Info,
                    label = "App info",
                    onClick = onOpenAppInfo
                )
                ContextMenuItem(
                    icon = Icons.Default.RemoveCircleOutline,
                    label = "Remove from Home",
                    onClick = onRemoveFromHome
                )
                ContextMenuItem(
                    icon = Icons.Default.Delete,
                    label = "Uninstall",
                    onClick = onUninstallApp
                )
            }
        }
    }
}

@Composable
fun ContextMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag(testTag)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF382F2D), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 16.sp, color = Color(0xFF231F20))
    }
}
