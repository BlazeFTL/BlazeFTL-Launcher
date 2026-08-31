package com.example

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

class MainActivity : ComponentActivity() {

    private lateinit var prefsRepo: LauncherPreferencesRepository
    private lateinit var appRepo: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefsRepo = LauncherPreferencesRepository(applicationContext)
        appRepo = AppRepository(applicationContext)

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
}

@Composable
fun SparkLauncherApp(
    prefsRepo: LauncherPreferencesRepository,
    appRepo: AppRepository,
    onOpenSystemSettings: () -> Unit
) {
    val settings by prefsRepo.settings.collectAsState()
    var currentScreen by remember { mutableStateOf(LauncherScreen.HOME) }
    var installedApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    val dockApps = remember { appRepo.getDockApps() }
    val homeScreenApps = remember { appRepo.getHomeScreenApps() }

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

    LaunchedEffect(Unit) {
        installedApps = appRepo.getInstalledApps()
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

    fun handleAppClick(app: AppItem) {
        if (app.packageName == "com.android.settings") {
            currentScreen = LauncherScreen.SETTINGS_MAIN
            return
        }
        val launched = appRepo.launchApp(app.packageName)
        if (!launched) {
            showToast("Opening ${app.label}")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                when {
                    targetState == LauncherScreen.APP_DRAWER ->
                        slideInVertically { it } togetherWith slideOutVertically { -it }
                    initialState == LauncherScreen.APP_DRAWER ->
                        slideInVertically { -it } togetherWith slideOutVertically { it }
                    targetState == LauncherScreen.RECENTS_OVERVIEW || initialState == LauncherScreen.RECENTS_OVERVIEW ->
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    else ->
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                }
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                LauncherScreen.HOME -> {
                    HomeScreen(
                        settings = settings,
                        homeApps = homeScreenApps,
                        dockApps = dockApps,
                        onAppClick = { handleAppClick(it) },
                        onOpenDrawer = { currentScreen = LauncherScreen.APP_DRAWER },
                        onOpenRecents = { currentScreen = LauncherScreen.RECENTS_OVERVIEW },
                        onOpenSettings = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                        onShowToast = { showToast(it) }
                    )
                }
                LauncherScreen.APP_DRAWER -> {
                    AppDrawerScreen(
                        settings = settings,
                        allApps = if (installedApps.isNotEmpty()) installedApps else homeScreenApps,
                        onAppClick = { handleAppClick(it) },
                        onCloseDrawer = { currentScreen = LauncherScreen.HOME },
                        onShowToast = { showToast(it) }
                    )
                }
                LauncherScreen.RECENTS_OVERVIEW -> {
                    RecentsOverviewScreen(
                        settings = settings,
                        recentApps = homeScreenApps.take(6),
                        onClose = { currentScreen = LauncherScreen.HOME },
                        onAppClick = { handleAppClick(it) },
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
                        onUpdate = { prefsRepo.updateSettings(it) },
                        onRestartLauncher = {
                            currentScreen = LauncherScreen.HOME
                            showToast("Spark Launcher reloaded")
                        },
                        onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN }
                    )
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
