package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.AppRepository
import com.example.data.LauncherPreferencesRepository
import com.example.model.AppItem
import com.example.model.LauncherScreen
import com.example.ui.components.LauncherToast
import com.example.ui.screens.AppDrawerScreen
import com.example.ui.screens.AppDrawerSettingsScreen
import com.example.ui.screens.GesturesSettingsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HomeScreenSettingsScreen
import com.example.ui.screens.HomeSettingsMainScreen
import com.example.ui.screens.IconsSettingsScreen
import com.example.ui.screens.MiscellaneousSettingsScreen
import com.example.ui.screens.RecentsOverviewScreen
import com.example.ui.screens.RecentsSettingsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    private lateinit var prefsRepo: LauncherPreferencesRepository
    private lateinit var appRepo: AppRepository
    private var packageReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        window.navigationBarColor = AndroidColor.TRANSPARENT
        window.statusBarColor = AndroidColor.TRANSPARENT

        prefsRepo = LauncherPreferencesRepository(applicationContext)
        appRepo = AppRepository(applicationContext)

        // As soon as launcher is launched, build installed app data immediately in background
        appRepo.buildInstalledAppDataAsync(lifecycleScope)

        val packageFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                appRepo.buildInstalledAppDataAsync(lifecycleScope)
            }
        }
        packageReceiver = receiver
        registerReceiver(receiver, packageFilter)

        setContent {
            MyApplicationTheme {
                SparkLauncherApp(
                    prefsRepo = prefsRepo,
                    appRepo = appRepo,
                    onOpenSystemSettings = {
                        try {
                            startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (e: Exception) {
                            // Ignored
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        packageReceiver?.let {
            try { unregisterReceiver(it) } catch (e: Exception) { }
        }
    }
}

@Composable
fun SparkLauncherApp(
    prefsRepo: LauncherPreferencesRepository,
    appRepo: AppRepository,
    onOpenSystemSettings: () -> Unit
) {
    val context = LocalContext.current
    val settings by prefsRepo.settings.collectAsState()
    var currentScreen by remember { mutableStateOf(LauncherScreen.HOME) }
    val repoInstalledApps by appRepo.installedAppsFlow.collectAsState()
    var installedApps by remember { mutableStateOf(appRepo.getPreloadedApps()) }
    val dockApps = remember(installedApps) { appRepo.getDockApps() }
    
    val homeAppsList = remember { mutableStateListOf<AppItem?>() }

    val buildProgress by appRepo.buildProgressFlow.collectAsState()
    var isProgressBannerDismissed by remember { mutableStateOf(false) }

    val visibleDrawerApps = remember(installedApps, homeAppsList.toList(), settings.hiddenAppPackages) {
        val rawList = if (installedApps.isNotEmpty()) installedApps else homeAppsList.filterNotNull()
        if (settings.hiddenAppPackages.isEmpty()) {
            rawList
        } else {
            rawList.filterNot { settings.hiddenAppPackages.contains(it.packageName) }
        }
    }

    var memoryInfoText by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf("") }
    var isToastVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun showToast(msg: String) {
        toastMessage = msg
        isToastVisible = true
        coroutineScope.launch {
            delay(2500)
            isToastVisible = false
        }
    }

    // Refresh installed apps and sync home screen items as soon as data is ready
    LaunchedEffect(repoInstalledApps) {
        if (repoInstalledApps.isNotEmpty()) {
            installedApps = repoInstalledApps
            val savedSlots = prefsRepo.getHomeScreenSlots()
            val appMap = repoInstalledApps.associateBy { it.packageName }
            if (savedSlots != null && savedSlots.isNotEmpty()) {
                val restored = savedSlots.map { pkg -> pkg?.let { appMap[it] } }
                homeAppsList.clear()
                homeAppsList.addAll(restored)
            } else {
                val defaultSlots = appRepo.getHomeScreenDefaultSlots()
                homeAppsList.clear()
                homeAppsList.addAll(defaultSlots.map { pkg -> pkg?.let { appMap[it] } })
            }
            memoryInfoText = appRepo.getFormattedMemoryInfo()
        }
    }

    var nowPlayingTrack by remember { mutableStateOf(com.example.model.NowPlayingTrack()) }

    LaunchedEffect(Unit) {
        while (true) {
            nowPlayingTrack = appRepo.getActiveMediaTrack()
            kotlinx.coroutines.delay(2000L)
        }
    }

    // Shake Sensor Detection
    DisposableEffect(settings.shakeGestureAction, settings.shakeGestureIntensity, currentScreen) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastShakeTime = 0L

        val threshold = when (settings.shakeGestureIntensity) {
            0 -> 18f // Light
            1 -> 24f // Medium
            2 -> 30f // Strong
            else -> 24f
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                val now = System.currentTimeMillis()
                if (acceleration > threshold && now - lastShakeTime > 1500L) {
                    lastShakeTime = now
                    appRepo.triggerHapticFeedback(settings.launcherVibrationIntensity)

                    if (currentScreen == LauncherScreen.RECENTS_OVERVIEW && settings.recentsShakeToClearAll) {
                        showToast("Recents cleared by shake gesture")
                    } else if (currentScreen == LauncherScreen.HOME) {
                        when (settings.shakeGestureAction) {
                            1 -> {
                                currentScreen = LauncherScreen.APP_DRAWER
                                if (settings.actionToasts) showToast("Opened App Drawer (Shake)")
                            }
                            2 -> {
                                currentScreen = LauncherScreen.RECENTS_OVERVIEW
                                if (settings.actionToasts) showToast("Opened Recents (Shake)")
                            }
                            3 -> {
                                currentScreen = LauncherScreen.SETTINGS_MAIN
                                if (settings.actionToasts) showToast("Opened Settings (Shake)")
                            }
                            4 -> {
                                if (settings.actionToasts) showToast("Screen locked (Shake)")
                            }
                            5 -> {
                                appRepo.openWallpaperPicker()
                                if (settings.actionToasts) showToast("Opened Wallpaper (Shake)")
                            }
                            6 -> {
                                val nonNullApps = homeAppsList.filterNotNull()
                                homeAppsList.clear()
                                homeAppsList.addAll(nonNullApps)
                                prefsRepo.saveHomeScreenSlots(homeAppsList.map { it?.packageName })
                                showToast("Empty spaces filled (Shake)")
                            }
                            7 -> {
                                val sorted = homeAppsList.filterNotNull().sortedBy { it.label.lowercase() }
                                homeAppsList.clear()
                                homeAppsList.addAll(sorted)
                                prefsRepo.saveHomeScreenSlots(homeAppsList.map { it?.packageName })
                                showToast("Rearranged by app name (Shake)")
                            }
                            8 -> {
                                val sorted = homeAppsList.filterNotNull().sortedWith(compareBy({ it.isSystemApp }, { it.label.lowercase() }))
                                homeAppsList.clear()
                                homeAppsList.addAll(sorted)
                                prefsRepo.saveHomeScreenSlots(homeAppsList.map { it?.packageName })
                                showToast("Rearranged by app type (Shake)")
                            }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Back handling
    BackHandler(enabled = currentScreen != LauncherScreen.HOME) {
        currentScreen = when (currentScreen) {
            LauncherScreen.SETTINGS_ICONS,
            LauncherScreen.SETTINGS_HOME_SCREEN,
            LauncherScreen.SETTINGS_GESTURES,
            LauncherScreen.SETTINGS_APP_DRAWER,
            LauncherScreen.SETTINGS_RECENTS,
            LauncherScreen.SETTINGS_MISCELLANEOUS -> LauncherScreen.SETTINGS_MAIN
            LauncherScreen.SETTINGS_MAIN -> LauncherScreen.HOME
            LauncherScreen.APP_DRAWER -> LauncherScreen.HOME
            LauncherScreen.RECENTS_OVERVIEW -> LauncherScreen.HOME
            LauncherScreen.HOME -> LauncherScreen.HOME
        }
    }

    val fillEmptySpaces: () -> Unit = {
        val nonNullApps = homeAppsList.filterNotNull()
        homeAppsList.clear()
        homeAppsList.addAll(nonNullApps)
        prefsRepo.saveHomeScreenSlots(homeAppsList.map { it?.packageName })
        showToast("Empty spaces filled")
    }

    val rearrangeByName: () -> Unit = {
        val sorted = homeAppsList.filterNotNull().sortedBy { it.label.lowercase() }
        homeAppsList.clear()
        homeAppsList.addAll(sorted)
        prefsRepo.saveHomeScreenSlots(homeAppsList.map { it?.packageName })
        showToast("Rearranged by app name (A - Z)")
    }

    val rearrangeByType: () -> Unit = {
        val sorted = homeAppsList.filterNotNull().sortedWith(compareBy({ it.isSystemApp }, { it.label.lowercase() }))
        homeAppsList.clear()
        homeAppsList.addAll(sorted)
        prefsRepo.saveHomeScreenSlots(homeAppsList.map { it?.packageName })
        showToast("Rearranged by app type")
    }

    val resetToSS2Layout: () -> Unit = {
        val defaultSlots = appRepo.getHomeScreenDefaultSlots()
        val allApps = appRepo.getPreloadedApps().associateBy { it.packageName }
        homeAppsList.clear()
        homeAppsList.addAll(defaultSlots.map { pkg -> pkg?.let { allApps[it] } })
        prefsRepo.saveHomeScreenSlots(defaultSlots)
        showToast("Reset to layout with empty spaces")
    }

    fun handleAppClick(app: AppItem) {
        if (settings.enableHapticOnRecents) {
            appRepo.triggerHapticFeedback(settings.launcherVibrationIntensity)
        }
        val launched = appRepo.launchApp(app.packageName, app.activityName)
        if (!launched) {
            showToast("Opening ${app.label}")
        }
    }

    fun addAppToHomeScreen(packageName: String) {
        val app = installedApps.find { it.packageName == packageName } ?: return
        if (!homeAppsList.any { it?.packageName == packageName }) {
            val emptyIndex = homeAppsList.indexOfFirst { it == null }
            if (emptyIndex != -1) {
                homeAppsList[emptyIndex] = app
            } else {
                homeAppsList.add(app)
            }
            prefsRepo.saveHomeScreenSlots(homeAppsList.map { it?.packageName })
            showToast("Added ${app.label} to Home Screen")
        } else {
            showToast("${app.label} is already on Home Screen")
        }
    }

    fun removeAppFromHomeScreen(packageName: String) {
        val index = homeAppsList.indexOfFirst { it?.packageName == packageName }
        if (index != -1) {
            val removed = homeAppsList[index]
            homeAppsList[index] = null
            prefsRepo.saveHomeScreenSlots(homeAppsList.map { it?.packageName })
            showToast("Removed ${removed?.label ?: "App"} from Home Screen")
        }
    }

    val isDrawerOpen = currentScreen == LauncherScreen.APP_DRAWER
    val drawerProgress by animateFloatAsState(
        targetValue = if (isDrawerOpen) 1f else 0f,
        animationSpec = if (isDrawerOpen) {
            tween(durationMillis = 390, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
        } else {
            tween(durationMillis = 280, easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f))
        },
        label = "DrawerAnimation"
    )

    val view = LocalView.current
    LaunchedEffect(isDrawerOpen, settings.drawerBackgroundOpacity) {
        val window = (view.context as? ComponentActivity)?.window
        if (window != null) {
            val isLightDrawerScrim = isDrawerOpen && settings.drawerBackgroundOpacity >= 40
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = isLightDrawerScrim
            insetsController.isAppearanceLightNavigationBars = isLightDrawerScrim
        }
    }

    BackHandler(enabled = isDrawerOpen) {
        currentScreen = LauncherScreen.HOME
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Base Home Screen (smoothly fades with GPU-accelerated graphicsLayer)
        HomeScreen(
            settings = settings,
            homeApps = homeAppsList,
            dockApps = dockApps,
            allApps = installedApps,
            nowPlayingTrack = nowPlayingTrack,
            onAppClick = { handleAppClick(it) },
            onOpenDrawer = { currentScreen = LauncherScreen.APP_DRAWER },
            onOpenRecents = {
                memoryInfoText = appRepo.getFormattedMemoryInfo()
                currentScreen = LauncherScreen.RECENTS_OVERVIEW
            },
            onOpenSettings = { currentScreen = LauncherScreen.SETTINGS_MAIN },
            onOpenWallpaper = { appRepo.openWallpaperPicker() },
            onOpenAppInfo = { appRepo.openAppInfo(it) },
            onUninstallApp = { appRepo.uninstallApp(it) },
            onRemoveFromHome = { removeAppFromHomeScreen(it) },
            onFillEmptySpaces = fillEmptySpaces,
            onRearrangeByName = rearrangeByName,
            onRearrangeByType = rearrangeByType,
            onResetToSS2Layout = resetToSS2Layout,
            onExpandQuickSettings = { appRepo.expandQuickSettings() },
            onShowToast = { showToast(it) },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = (1f - drawerProgress * 1.4f).coerceIn(0f, 1f)
                    translationY = -drawerProgress * 80f
                    scaleX = 1f - (drawerProgress * 0.03f)
                    scaleY = 1f - (drawerProgress * 0.03f)
                }
        )

        // Fullscreen Translucent Scrim over system wallpaper (GPU-accelerated RenderNode)
        val overlayAlpha = (settings.drawerBackgroundOpacity / 100f).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = (overlayAlpha * drawerProgress).coerceIn(0f, 1f)
                }
                .background(Color(0xFFE2E7EC))
        )

        // App Drawer Sliding Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = (1f - drawerProgress) * size.height
                    alpha = if (drawerProgress <= 0.001f) 0f else 1f
                }
        ) {
            if (drawerProgress > 0.001f || isDrawerOpen) {
                AppDrawerScreen(
                    settings = settings,
                    allApps = visibleDrawerApps,
                    onAppClick = { handleAppClick(it) },
                    onAddToHome = { addAppToHomeScreen(it) },
                    onOpenAppInfo = { appRepo.openAppInfo(it) },
                    onUninstallApp = { appRepo.uninstallApp(it) },
                    onCloseDrawer = { currentScreen = LauncherScreen.HOME },
                    onShowToast = { showToast(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Dismissible Progress UI at top after first install showing app list/icon building
        AnimatedVisibility(
            visible = buildProgress.isBuilding && !isProgressBannerDismissed,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .zIndex(20f)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = Color(0xFFE85D54),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Building app list & icons",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "${buildProgress.current} of ${buildProgress.total} completed",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        IconButton(
                            onClick = { isProgressBannerDismissed = true },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { buildProgress.progress },
                        color = Color(0xFFE85D54),
                        trackColor = Color(0xFF334155),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }

        // Overlay for Settings and Recents screens
        AnimatedVisibility(
            visible = currentScreen != LauncherScreen.HOME && currentScreen != LauncherScreen.APP_DRAWER,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(150))
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { it / 4 } + fadeIn(tween(180)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(180, easing = FastOutLinearInEasing)) { -it / 4 } + fadeOut(tween(140)))
                },
                label = "SettingsRecentsTransition"
            ) { screen ->
                when (screen) {
                    LauncherScreen.RECENTS_OVERVIEW -> {
                        RecentsOverviewScreen(
                            settings = settings,
                            recentApps = if (homeAppsList.isNotEmpty()) homeAppsList.filterNotNull().take(6) else appRepo.getHomeScreenApps().take(6),
                            memoryInfo = memoryInfoText,
                            onClose = { currentScreen = LauncherScreen.HOME },
                            onAppClick = { handleAppClick(it) },
                            onKillProcess = { appRepo.killBackgroundProcesses(it) },
                            onShowToast = { showToast(it) }
                        )
                    }
                    LauncherScreen.SETTINGS_MAIN -> {
                        HomeSettingsMainScreen(
                            onNavigate = { currentScreen = it },
                            onBack = { currentScreen = LauncherScreen.HOME }
                        )
                    }
                    LauncherScreen.SETTINGS_ICONS -> {
                        IconsSettingsScreen(
                            settings = settings,
                            onUpdate = { prefsRepo.updateSettings(it) },
                            onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN }
                        )
                    }
                    LauncherScreen.SETTINGS_HOME_SCREEN -> {
                        HomeScreenSettingsScreen(
                            settings = settings,
                            onUpdate = { prefsRepo.updateSettings(it) },
                            onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN }
                        )
                    }
                    LauncherScreen.SETTINGS_GESTURES -> {
                        GesturesSettingsScreen(
                            settings = settings,
                            onUpdate = { prefsRepo.updateSettings(it) },
                            onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN }
                        )
                    }
                    LauncherScreen.SETTINGS_APP_DRAWER -> {
                        AppDrawerSettingsScreen(
                            settings = settings,
                            onUpdate = { prefsRepo.updateSettings(it) },
                            onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN }
                        )
                    }
                    LauncherScreen.SETTINGS_RECENTS -> {
                        RecentsSettingsScreen(
                            settings = settings,
                            onUpdate = { prefsRepo.updateSettings(it) },
                            onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN }
                        )
                    }
                    LauncherScreen.SETTINGS_MISCELLANEOUS -> {
                        MiscellaneousSettingsScreen(
                            settings = settings,
                            allApps = if (installedApps.isNotEmpty()) installedApps else homeAppsList.filterNotNull(),
                            onUpdate = { prefsRepo.updateSettings(it) },
                            onRestartLauncher = {
                                currentScreen = LauncherScreen.HOME
                                showToast("Spark Launcher reloaded")
                            },
                            onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN }
                        )
                    }
                    else -> Unit
                }
            }
        }

        // Floating Toast Notification
        LauncherToast(
            message = toastMessage,
            visible = isToastVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 60.dp)
        )
    }
}
