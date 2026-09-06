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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.Shape
import com.example.util.IconShapeHelper
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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.platform.LocalContext
import android.os.BatteryManager
import android.content.IntentFilter
import java.util.Calendar
import com.example.model.DesktopItem
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
    homeSlots: List<DesktopItem?> = emptyList(),
    homeApps: List<AppItem?> = emptyList(),
    dockApps: List<AppItem>,
    allApps: List<AppItem> = emptyList(),
    nowPlayingTrack: com.example.model.NowPlayingTrack = com.example.model.NowPlayingTrack(),
    onAppClick: (AppItem) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenRecents: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWallpaper: () -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onRemoveFromHome: (String) -> Unit,
    onFillEmptySpaces: () -> Unit = {},
    onRearrangeByName: () -> Unit = {},
    onRearrangeByType: () -> Unit = {},
    onResetToSS2Layout: () -> Unit = {},
    onExpandQuickSettings: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDesktopMenu by remember { mutableStateOf(false) }
    var selectedAppForPopup by remember { mutableStateOf<AppItem?>(null) }
    var openFolder by remember { mutableStateOf<DesktopItem.Folder?>(null) }
    val iconShape = remember(settings.iconShape) { IconShapeHelper.getShape(settings.iconShape) }

    val resolvedDesktopSlots = remember(homeSlots, homeApps) {
        if (homeSlots.isNotEmpty()) {
            homeSlots
        } else {
            homeApps.map { it?.let { app -> DesktopItem.App(app) } }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -18f) {
                        onOpenDrawer()
                    } else if (dragAmount > 36f) {
                        onExpandQuickSettings()
                    }
                }
            }
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
                    nowPlayingTrack = nowPlayingTrack,
                    onSongClick = {
                        if (nowPlayingTrack.isPlaying) {
                            if (nowPlayingTrack.packageName.isNotBlank()) {
                                val musicApp = allApps.find { it.packageName == nowPlayingTrack.packageName }
                                if (musicApp != null) {
                                    onAppClick(musicApp)
                                } else {
                                    onShowToast("Playing: ${nowPlayingTrack.title}")
                                }
                            } else {
                                onShowToast("Playing: ${nowPlayingTrack.title}")
                            }
                        } else {
                            onShowToast("Calendar & At-A-Glance")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            // Middle screen area: also handles full-screen swipe up anywhere
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -15f) {
                                onOpenDrawer()
                            } else if (dragAmount > 30f) {
                                onExpandQuickSettings()
                            }
                        }
                    }
            )

            // Desktop App Grid (Responsive 6 columns)
            val iconScale = (settings.iconSizePercent / 100f).coerceIn(0.5f, 1.5f)
            val iconBaseDp = (48 * iconScale).dp
            val fontSizeSp = (11.5f * (settings.fontSizePercent / 100f)).sp

            if (resolvedDesktopSlots.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -18f) {
                                    onOpenDrawer()
                                }
                            }
                        }
                ) {
                    itemsIndexed(
                        items = resolvedDesktopSlots,
                        key = { index, item ->
                            when (item) {
                                is DesktopItem.App -> item.app.uniqueKey
                                is DesktopItem.Folder -> item.id
                                null -> "empty_slot_$index"
                            }
                        },
                        contentType = { _, item ->
                            when (item) {
                                is DesktopItem.App -> "desktop_app"
                                is DesktopItem.Folder -> "desktop_folder"
                                null -> "empty_slot"
                            }
                        }
                    ) { index, item ->
                        when (item) {
                            is DesktopItem.App -> {
                                DesktopAppIcon(
                                    app = item.app,
                                    iconSizeDp = iconBaseDp,
                                    fontSizeSp = fontSizeSp,
                                    showLabel = settings.iconLabelsOnDesktop,
                                    maxLines = settings.maxLabelLines,
                                    iconShape = iconShape,
                                    forceMonochrome = settings.forceMonochrome && settings.themedIcons,
                                    showNotificationDot = settings.notificationDots,
                                    onClick = { onAppClick(item.app) },
                                    onLongClick = {
                                        if (settings.lockLayout) {
                                            onShowToast("Desktop layout is locked in settings")
                                        } else {
                                            selectedAppForPopup = item.app
                                        }
                                    }
                                )
                            }
                            is DesktopItem.Folder -> {
                                DesktopFolderIcon(
                                    title = item.title,
                                    apps = item.apps,
                                    iconSizeDp = iconBaseDp,
                                    fontSizeSp = fontSizeSp,
                                    showLabel = settings.iconLabelsOnDesktop,
                                    maxLines = settings.maxLabelLines,
                                    iconShape = iconShape,
                                    onClick = { openFolder = item },
                                    onLongClick = { onShowToast("Folder: ${item.title}") }
                                )
                            }
                            null -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(iconBaseDp + 24.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Hotseat Dock (Apps placed ABOVE bottom search bar)
            HotseatDock(
                settings = settings,
                dockApps = dockApps,
                iconSizeDp = iconBaseDp,
                iconShape = iconShape,
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
                onFillEmptySpaces = {
                    showDesktopMenu = false
                    onFillEmptySpaces()
                },
                onRearrangeByName = {
                    showDesktopMenu = false
                    onRearrangeByName()
                },
                onRearrangeByType = {
                    showDesktopMenu = false
                    onRearrangeByType()
                },
                onResetToSS2Layout = {
                    showDesktopMenu = false
                    onResetToSS2Layout()
                },
                onShowToast = onShowToast
            )
        }

        // Open Folder Dialog (Matches Spark Launcher in SS 3 & 8)
        if (openFolder != null) {
            val folder = openFolder!!
            Dialog(onDismissRequest = { openFolder = null }) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = folder.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(folder.apps) { app ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            openFolder = null
                                            onAppClick(app)
                                        }
                                        .padding(4.dp)
                                ) {
                                    AppIconBadge(app = app, sizeDp = 48.dp, shape = iconShape)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = app.label,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color(0xFF1E293B),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // App Item Long Press Popup
        if (selectedAppForPopup != null) {
            val app = selectedAppForPopup!!
            AppItemContextMenu(
                app = app,
                iconShape = iconShape,
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
    nowPlayingTrack: com.example.model.NowPlayingTrack,
    onSongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fullDateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
    var currentFullDate by remember { mutableStateOf(fullDateFormat.format(Date())) }
    var currentHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentFullDate = fullDateFormat.format(now)
            currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            delay(10000L)
        }
    }

    val greetingText = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good morning."
            in 12..16 -> "Good afternoon."
            in 17..20 -> "Good evening."
            else -> "Good night."
        }
    }

    val context = LocalContext.current
    val statusText = remember(currentHour) {
        val batteryIntent = try {
            context.registerReceiver(null, IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        } catch (e: Exception) { null }
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        when {
            isCharging && pct > 0 -> "⚡ Charging • $pct%"
            currentHour >= 21 || currentHour < 4 -> "⚡ Remember to full charge me before you sleep."
            currentHour in 17..20 -> "⚡ Enjoy the night."
            currentHour in 5..11 -> "⚡ Have a wonderful day ahead."
            else -> "⚡ Stay inspired and productive."
        }
    }

    val widgetShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.55f),
            offset = Offset(0f, 2f),
            blurRadius = 8f
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onSongClick() })
            }
            .padding(vertical = 4.dp)
    ) {
        // Line 1: Time Greeting (Matches Spark Launcher in SS 3 & 8)
        Text(
            text = greetingText,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
            style = androidx.compose.ui.text.TextStyle(shadow = widgetShadow)
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Line 2: Full Date (e.g. "It's Sunday, September 6")
        Text(
            text = "It's $currentFullDate",
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            style = androidx.compose.ui.text.TextStyle(shadow = widgetShadow)
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Line 3: Charging / Night status message
        Text(
            text = statusText,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            style = androidx.compose.ui.text.TextStyle(shadow = widgetShadow)
        )

        // If music is actively playing, show sleek compact Now Playing subline
        if (settings.nowPlaying && nowPlayingTrack.isPlaying && nowPlayingTrack.title.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Playing",
                        tint = Color(0xFFFF8A80),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (nowPlayingTrack.artist.isNotBlank()) "${nowPlayingTrack.title} • ${nowPlayingTrack.artist}" else nowPlayingTrack.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(12.dp)
                    ) {
                        Box(modifier = Modifier.width(2.5.dp).height(8.dp).background(Color(0xFFFF8A80), RoundedCornerShape(1.dp)))
                        Box(modifier = Modifier.width(2.5.dp).height(12.dp).background(Color(0xFFFF8A80), RoundedCornerShape(1.dp)))
                        Box(modifier = Modifier.width(2.5.dp).height(5.dp).background(Color(0xFFFF8A80), RoundedCornerShape(1.dp)))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopFolderIcon(
    title: String,
    apps: List<AppItem>,
    iconSizeDp: androidx.compose.ui.unit.Dp,
    fontSizeSp: androidx.compose.ui.unit.TextUnit,
    showLabel: Boolean,
    maxLines: Int,
    iconShape: Shape = CircleShape,
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
        // Frosted Translucent Circle Container matching Spark Launcher (Screenshot 3 & 8)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(iconSizeDp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.28f))
                .border(1.dp, Color.White.copy(alpha = 0.40f), CircleShape)
                .padding(6.dp)
        ) {
            val miniSize = (iconSizeDp.value * 0.35f).dp
            val displayApps = apps.take(4)

            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    displayApps.getOrNull(0)?.let {
                        AppIconBadge(app = it, sizeDp = miniSize, shape = CircleShape)
                    } ?: Box(modifier = Modifier.size(miniSize))
                    displayApps.getOrNull(1)?.let {
                        AppIconBadge(app = it, sizeDp = miniSize, shape = CircleShape)
                    } ?: Box(modifier = Modifier.size(miniSize))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    displayApps.getOrNull(2)?.let {
                        AppIconBadge(app = it, sizeDp = miniSize, shape = CircleShape)
                    } ?: Box(modifier = Modifier.size(miniSize))
                    displayApps.getOrNull(3)?.let {
                        AppIconBadge(app = it, sizeDp = miniSize, shape = CircleShape)
                    } ?: Box(modifier = Modifier.size(miniSize))
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopAppIcon(
    app: AppItem,
    iconSizeDp: androidx.compose.ui.unit.Dp,
    fontSizeSp: androidx.compose.ui.unit.TextUnit,
    showLabel: Boolean,
    maxLines: Int,
    iconShape: Shape = CircleShape,
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
            shape = iconShape,
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
    iconShape: Shape = CircleShape,
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
                    iconShape = iconShape,
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
    onFillEmptySpaces: () -> Unit = {},
    onRearrangeByName: () -> Unit = {},
    onRearrangeByType: () -> Unit = {},
    onResetToSS2Layout: () -> Unit = {},
    onShowToast: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(0.92f)
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
                    icon = Icons.Default.AutoAwesome,
                    label = "Fill Empty Spaces (Compact Apps)",
                    onClick = onFillEmptySpaces,
                    testTag = "menu_fill_empty_spaces"
                )
                ContextMenuItem(
                    icon = Icons.Default.SortByAlpha,
                    label = "Rearrange by App Name (A - Z)",
                    onClick = onRearrangeByName,
                    testTag = "menu_rearrange_name"
                )
                ContextMenuItem(
                    icon = Icons.Default.Category,
                    label = "Rearrange by App Category",
                    onClick = onRearrangeByType,
                    testTag = "menu_rearrange_type"
                )
                ContextMenuItem(
                    icon = Icons.Default.Restore,
                    label = "Reset to Reference Layout (SS 2)",
                    onClick = onResetToSS2Layout,
                    testTag = "menu_reset_ss2"
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
    iconShape: Shape = CircleShape,
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
                    AppIconBadge(app = app, sizeDp = 40.dp, shape = iconShape)
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
