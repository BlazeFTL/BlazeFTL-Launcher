package com.example.model

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

data class AppItem(
    val packageName: String,
    val activityName: String = "",
    val label: String,
    val isSystemApp: Boolean = false,
    val iconDrawable: Drawable? = null,
    val iconBitmap: ImageBitmap? = null,
    val iconVector: ImageVector? = null,
    val iconColor: Long = 0xFF4A90E2,
    val isThemed: Boolean = false,
    val category: String = "Productivity"
)

data class LauncherSettings(
    // Icons
    val iconPack: String = "Default",
    val themedIcons: Boolean = false,
    val themedIconsInDrawer: Boolean = false,
    val forceMonochrome: Boolean = true,
    val notificationDots: Boolean = false,
    val iconSizePercent: Int = 90,
    val fontSizePercent: Int = 95,
    val maxLabelLines: Int = 1,

    // Home Screen - General
    val lockLayout: Boolean = false,
    val addIconsToHome: Boolean = true,
    val doubleTapToSleep: Boolean = true,
    val wallpaperScrolling: Boolean = false,
    val wallpaperZooming: Boolean = true,
    val allowShortParallax: Boolean = false,
    val singlePageCenter: Boolean = false,

    // Home Screen - Interface
    val swipeToGoogle: Boolean = false,
    val showStatusBar: Boolean = true,
    val statusBarShadow: Boolean = true,
    val iconLabelsOnDesktop: Boolean = true,
    val hotseatBackground: Boolean = false,
    val hotseatBackgroundOpacity: Int = 40,

    // Home Screen - Quickspace
    val atAGlance: Boolean = true,
    val randomMessages: Boolean = true,
    val extendedStyle: Boolean = true,
    val nowPlaying: Boolean = true,
    val weatherCondition: Boolean = false,
    val currentCity: Boolean = false,
    val detailedWeather: Boolean = false,

    // Home Screen - Search bar
    val googleSearchBar: Boolean = false,
    val musicSearch: Boolean = false,
    val themedHotseatSearchBar: Boolean = true,
    val monochromeSearchTheme: Boolean = true,
    val searchBarCornerRadius: Int = 100,

    // App Drawer
    val appSearchBar: Boolean = true,
    val iconLabelsInDrawer: Boolean = true,
    val drawerRowHeight: Int = 75,
    val drawerBackgroundOpacity: Int = 80,

    // Gestures
    val shakeGestureAction: Int = 0,
    val shakeGestureIntensity: Int = 4,

    // Recents
    val recentsMemoryInfo: Boolean = true,
    val recentsBackgroundOpacity: Int = 40,
    val recentsClearAll: Boolean = true,
    val shakeToClearRecentsIntensity: Int = 4,
    val recentsKillApp: Boolean = true,
    val recentsLens: Boolean = true,
    val recentsPinApp: Boolean = true,
    val recentsScreenshot: Boolean = true,
    val recentsSplitApp: Boolean = true,
    val recentsShakeToClearAll: Boolean = false,

    // Miscellaneous
    val useTaskbar: Boolean = false,
    val allowHomeScreenRotation: Boolean = false,
    val enableHapticOnRecents: Boolean = true,
    val launcherVibrationIntensity: Int = 0,
    val actionToasts: Boolean = true,
    val hiddenAppPackages: Set<String> = emptySet()
)

enum class LauncherScreen {
    HOME,
    APP_DRAWER,
    RECENTS_OVERVIEW,
    SETTINGS_MAIN,
    SETTINGS_ICONS,
    SETTINGS_HOME_SCREEN,
    SETTINGS_GESTURES,
    SETTINGS_APP_DRAWER,
    SETTINGS_RECENTS,
    SETTINGS_MISCELLANEOUS
}
