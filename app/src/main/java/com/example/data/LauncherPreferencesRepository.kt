package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("spark_launcher_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    private fun loadSettings(): LauncherSettings {
        return LauncherSettings(
            iconPack = prefs.getString("iconPack", "Default") ?: "Default",
            themedIcons = prefs.getBoolean("themedIcons", false),
            themedIconsInDrawer = prefs.getBoolean("themedIconsInDrawer", false),
            forceMonochrome = prefs.getBoolean("forceMonochrome", true),
            notificationDots = prefs.getBoolean("notificationDots", false),
            iconSizePercent = prefs.getInt("iconSizePercent", 90),
            fontSizePercent = prefs.getInt("fontSizePercent", 95),
            maxLabelLines = prefs.getInt("maxLabelLines", 1),

            lockLayout = prefs.getBoolean("lockLayout", false),
            addIconsToHome = prefs.getBoolean("addIconsToHome", true),
            doubleTapToSleep = prefs.getBoolean("doubleTapToSleep", true),
            wallpaperScrolling = prefs.getBoolean("wallpaperScrolling", false),
            wallpaperZooming = prefs.getBoolean("wallpaperZooming", true),
            allowShortParallax = prefs.getBoolean("allowShortParallax", false),
            singlePageCenter = prefs.getBoolean("singlePageCenter", false),

            swipeToGoogle = prefs.getBoolean("swipeToGoogle", false),
            showStatusBar = prefs.getBoolean("showStatusBar", true),
            statusBarShadow = prefs.getBoolean("statusBarShadow", true),
            iconLabelsOnDesktop = prefs.getBoolean("iconLabelsOnDesktop", true),
            hotseatBackground = prefs.getBoolean("hotseatBackground", false),
            hotseatBackgroundOpacity = prefs.getInt("hotseatBackgroundOpacity", 40),

            atAGlance = prefs.getBoolean("atAGlance", true),
            randomMessages = prefs.getBoolean("randomMessages", true),
            extendedStyle = prefs.getBoolean("extendedStyle", true),
            nowPlaying = prefs.getBoolean("nowPlaying", true),
            weatherCondition = prefs.getBoolean("weatherCondition", false),
            currentCity = prefs.getBoolean("currentCity", false),
            detailedWeather = prefs.getBoolean("detailedWeather", false),

            googleSearchBar = prefs.getBoolean("googleSearchBar", false),
            musicSearch = prefs.getBoolean("musicSearch", false),
            themedHotseatSearchBar = prefs.getBoolean("themedHotseatSearchBar", true),
            monochromeSearchTheme = prefs.getBoolean("monochromeSearchTheme", true),
            searchBarCornerRadius = prefs.getInt("searchBarCornerRadius", 100),

            appSearchBar = prefs.getBoolean("appSearchBar", true),
            iconLabelsInDrawer = prefs.getBoolean("iconLabelsInDrawer", true),
            drawerRowHeight = prefs.getInt("drawerRowHeight", 75),
            drawerBackgroundOpacity = prefs.getInt("drawerBackgroundOpacity", 80),

            shakeGestureAction = prefs.getInt("shakeGestureAction", 0),
            shakeGestureIntensity = prefs.getInt("shakeGestureIntensity", 4),

            recentsMemoryInfo = prefs.getBoolean("recentsMemoryInfo", true),
            recentsBackgroundOpacity = prefs.getInt("recentsBackgroundOpacity", 40),
            recentsClearAll = prefs.getBoolean("recentsClearAll", true),
            shakeToClearRecentsIntensity = prefs.getInt("shakeToClearRecentsIntensity", 4),
            recentsKillApp = prefs.getBoolean("recentsKillApp", true),
            recentsLens = prefs.getBoolean("recentsLens", true),
            recentsPinApp = prefs.getBoolean("recentsPinApp", true),
            recentsScreenshot = prefs.getBoolean("recentsScreenshot", true),
            recentsSplitApp = prefs.getBoolean("recentsSplitApp", true),
            recentsShakeToClearAll = prefs.getBoolean("recentsShakeToClearAll", false),

            useTaskbar = prefs.getBoolean("useTaskbar", false),
            allowHomeScreenRotation = prefs.getBoolean("allowHomeScreenRotation", false),
            enableHapticOnRecents = prefs.getBoolean("enableHapticOnRecents", true),
            launcherVibrationIntensity = prefs.getInt("launcherVibrationIntensity", 0),
            actionToasts = prefs.getBoolean("actionToasts", true),
            hiddenAppPackages = prefs.getStringSet("hiddenAppPackages", emptySet()) ?: emptySet()
        )
    }

    fun updateSettings(newSettings: LauncherSettings) {
        _settings.value = newSettings
        prefs.edit().apply {
            putString("iconPack", newSettings.iconPack)
            putBoolean("themedIcons", newSettings.themedIcons)
            putBoolean("themedIconsInDrawer", newSettings.themedIconsInDrawer)
            putBoolean("forceMonochrome", newSettings.forceMonochrome)
            putBoolean("notificationDots", newSettings.notificationDots)
            putInt("iconSizePercent", newSettings.iconSizePercent)
            putInt("fontSizePercent", newSettings.fontSizePercent)
            putInt("maxLabelLines", newSettings.maxLabelLines)

            putBoolean("lockLayout", newSettings.lockLayout)
            putBoolean("addIconsToHome", newSettings.addIconsToHome)
            putBoolean("doubleTapToSleep", newSettings.doubleTapToSleep)
            putBoolean("wallpaperScrolling", newSettings.wallpaperScrolling)
            putBoolean("wallpaperZooming", newSettings.wallpaperZooming)
            putBoolean("allowShortParallax", newSettings.allowShortParallax)
            putBoolean("singlePageCenter", newSettings.singlePageCenter)

            putBoolean("swipeToGoogle", newSettings.swipeToGoogle)
            putBoolean("showStatusBar", newSettings.showStatusBar)
            putBoolean("statusBarShadow", newSettings.statusBarShadow)
            putBoolean("iconLabelsOnDesktop", newSettings.iconLabelsOnDesktop)
            putBoolean("hotseatBackground", newSettings.hotseatBackground)
            putInt("hotseatBackgroundOpacity", newSettings.hotseatBackgroundOpacity)

            putBoolean("atAGlance", newSettings.atAGlance)
            putBoolean("randomMessages", newSettings.randomMessages)
            putBoolean("extendedStyle", newSettings.extendedStyle)
            putBoolean("nowPlaying", newSettings.nowPlaying)
            putBoolean("weatherCondition", newSettings.weatherCondition)
            putBoolean("currentCity", newSettings.currentCity)
            putBoolean("detailedWeather", newSettings.detailedWeather)

            putBoolean("googleSearchBar", newSettings.googleSearchBar)
            putBoolean("musicSearch", newSettings.musicSearch)
            putBoolean("themedHotseatSearchBar", newSettings.themedHotseatSearchBar)
            putBoolean("monochromeSearchTheme", newSettings.monochromeSearchTheme)
            putInt("searchBarCornerRadius", newSettings.searchBarCornerRadius)

            putBoolean("appSearchBar", newSettings.appSearchBar)
            putBoolean("iconLabelsInDrawer", newSettings.iconLabelsInDrawer)
            putInt("drawerRowHeight", newSettings.drawerRowHeight)
            putInt("drawerBackgroundOpacity", newSettings.drawerBackgroundOpacity)

            putInt("shakeGestureAction", newSettings.shakeGestureAction)
            putInt("shakeGestureIntensity", newSettings.shakeGestureIntensity)

            putBoolean("recentsMemoryInfo", newSettings.recentsMemoryInfo)
            putInt("recentsBackgroundOpacity", newSettings.recentsBackgroundOpacity)
            putBoolean("recentsClearAll", newSettings.recentsClearAll)
            putInt("shakeToClearRecentsIntensity", newSettings.shakeToClearRecentsIntensity)
            putBoolean("recentsKillApp", newSettings.recentsKillApp)
            putBoolean("recentsLens", newSettings.recentsLens)
            putBoolean("recentsPinApp", newSettings.recentsPinApp)
            putBoolean("recentsScreenshot", newSettings.recentsScreenshot)
            putBoolean("recentsSplitApp", newSettings.recentsSplitApp)
            putBoolean("recentsShakeToClearAll", newSettings.recentsShakeToClearAll)

            putBoolean("useTaskbar", newSettings.useTaskbar)
            putBoolean("allowHomeScreenRotation", newSettings.allowHomeScreenRotation)
            putBoolean("enableHapticOnRecents", newSettings.enableHapticOnRecents)
            putInt("launcherVibrationIntensity", newSettings.launcherVibrationIntensity)
            putBoolean("actionToasts", newSettings.actionToasts)
            putStringSet("hiddenAppPackages", newSettings.hiddenAppPackages)
            apply()
        }
    }

    fun getHomeScreenPackages(): List<String>? {
        val saved = prefs.getString("home_screen_packages", null) ?: return null
        return saved.split(",").filter { it.isNotBlank() }
    }

    fun saveHomeScreenPackages(packages: List<String>) {
        prefs.edit().putString("home_screen_packages", packages.joinToString(",")).apply()
    }
}
