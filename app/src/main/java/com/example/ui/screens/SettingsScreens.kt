package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.RecentActors
import androidx.compose.material.icons.outlined.ViewArray
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.AppItem
import com.example.model.LauncherScreen
import com.example.model.LauncherSettings
import com.example.ui.components.AppIconBadge
import com.example.ui.components.CoralAccent
import com.example.ui.components.SectionHeader
import com.example.ui.components.SettingNavigationItem
import com.example.ui.components.SettingSliderItem
import com.example.ui.components.SettingToggleItem
import com.example.ui.components.SettingsBgColor
import com.example.ui.components.SettingsCardContainer
import com.example.ui.components.SettingsTopBar

@Composable
fun HomeSettingsMainScreen(
    onNavigate: (LauncherScreen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SettingsBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsTopBar(
            title = "Home settings",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCardContainer {
            SettingNavigationItem(
                title = "Icons",
                subtitle = "Set icon size and more",
                icon = Icons.Outlined.Cookie,
                onClick = { onNavigate(LauncherScreen.SETTINGS_ICONS) },
                modifier = Modifier.testTag("nav_icons_settings")
            )
        }

        SettingsCardContainer {
            SettingNavigationItem(
                title = "Home screen",
                subtitle = "Set Google now panel and more",
                icon = Icons.Outlined.Home,
                onClick = { onNavigate(LauncherScreen.SETTINGS_HOME_SCREEN) },
                modifier = Modifier.testTag("nav_homescreen_settings")
            )
        }

        SettingsCardContainer {
            SettingNavigationItem(
                title = "Gestures settings",
                subtitle = "Configure shake gesture settings",
                icon = Icons.Outlined.PanTool,
                onClick = { onNavigate(LauncherScreen.SETTINGS_GESTURES) },
                modifier = Modifier.testTag("nav_gestures_settings")
            )
        }

        SettingsCardContainer {
            SettingNavigationItem(
                title = "App drawer",
                subtitle = "Customize your app drawer",
                icon = Icons.Outlined.GridView,
                onClick = { onNavigate(LauncherScreen.SETTINGS_APP_DRAWER) },
                modifier = Modifier.testTag("nav_drawer_settings")
            )
        }

        SettingsCardContainer {
            SettingNavigationItem(
                title = "Recents",
                subtitle = "Revamp the overview screen",
                icon = Icons.Outlined.ViewArray,
                onClick = { onNavigate(LauncherScreen.SETTINGS_RECENTS) },
                modifier = Modifier.testTag("nav_recents_settings")
            )
        }

        SettingsCardContainer {
            SettingNavigationItem(
                title = "Miscellaneous",
                subtitle = "Other options",
                icon = Icons.Outlined.Code,
                onClick = { onNavigate(LauncherScreen.SETTINGS_MISCELLANEOUS) },
                modifier = Modifier.testTag("nav_misc_settings")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun IconsSettingsScreen(
    settings: LauncherSettings,
    onUpdate: (LauncherSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showIconPackDialog by remember { mutableStateOf(false) }
    var showThemedIconsDialog by remember { mutableStateOf(false) }

    val iconPacks = listOf(
        "Default" to "System default app icons",
        "Material You" to "Adapts to wallpaper color scheme",
        "Monochrome" to "Minimalist black & white outline icons",
        "Pixel Circles" to "Uniform circular badge icons",
        "Vibrant Pop" to "High-contrast colorful modern icons"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SettingsBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsTopBar(
            title = "Icons",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCardContainer {
            SettingNavigationItem(
                title = "Icon pack",
                subtitle = settings.iconPack,
                onClick = { showIconPackDialog = true }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingNavigationItem(
                title = "Themed Icons",
                subtitle = if (settings.themedIcons) "Enabled" else "Disabled",
                onClick = { showThemedIconsDialog = true }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Apply themed icons to app drawer",
                subtitle = "Follow themed icons used on home screen",
                checked = settings.themedIconsInDrawer,
                onCheckedChange = { onUpdate(settings.copy(themedIconsInDrawer = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Force Monochrome themed icons",
                subtitle = "Generate monochromatic icons, if it is not provided by the app (requires re-toggling of themed icons)",
                checked = settings.forceMonochrome,
                onCheckedChange = { onUpdate(settings.copy(forceMonochrome = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingNavigationItem(
                title = "Notification dots",
                subtitle = if (settings.notificationDots) "Enabled" else "Disabled (Tap to toggle)",
                onClick = { onUpdate(settings.copy(notificationDots = !settings.notificationDots)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingSliderItem(
                title = "Icon size",
                value = settings.iconSizePercent,
                valueRange = 50f..150f,
                displayValue = "Value: ${settings.iconSizePercent} % ↺",
                onValueChange = { onUpdate(settings.copy(iconSizePercent = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingSliderItem(
                title = "Font size",
                value = settings.fontSizePercent,
                valueRange = 50f..150f,
                displayValue = "Value: ${settings.fontSizePercent} % ↺",
                onValueChange = { onUpdate(settings.copy(fontSizePercent = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingSliderItem(
                title = "Max lines for app label",
                value = settings.maxLabelLines,
                valueRange = 1f..3f,
                displayValue = "Value: ${settings.maxLabelLines} (by default)",
                onValueChange = { onUpdate(settings.copy(maxLabelLines = it)) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showIconPackDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showIconPackDialog = false }) {
            androidx.compose.material3.Card(
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Choose Icon Pack",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF231F20)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    iconPacks.forEach { (name, desc) ->
                        val isSelected = settings.iconPack == name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onUpdate(settings.copy(iconPack = name))
                                    showIconPackDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onUpdate(settings.copy(iconPack = name))
                                    showIconPackDialog = false
                                },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = com.example.ui.components.CoralAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = name,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color(0xFF231F20)
                                )
                                Text(
                                    text = desc,
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.material3.TextButton(onClick = { showIconPackDialog = false }) {
                            Text("Cancel", color = com.example.ui.components.CoralAccent)
                        }
                    }
                }
            }
        }
    }

    if (showThemedIconsDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showThemedIconsDialog = false }) {
            androidx.compose.material3.Card(
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Themed Icons",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF231F20)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val options = listOf(
                        true to ("Enabled" to "Tint icons with wallpaper accents and monochrome styling"),
                        false to ("Disabled" to "Use standard full-color icons")
                    )

                    options.forEach { (enabled, info) ->
                        val isSelected = settings.themedIcons == enabled
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onUpdate(settings.copy(themedIcons = enabled))
                                    showThemedIconsDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onUpdate(settings.copy(themedIcons = enabled))
                                    showThemedIconsDialog = false
                                },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = com.example.ui.components.CoralAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = info.first,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color(0xFF231F20)
                                )
                                Text(
                                    text = info.second,
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.material3.TextButton(onClick = { showThemedIconsDialog = false }) {
                            Text("Cancel", color = com.example.ui.components.CoralAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenSettingsScreen(
    settings: LauncherSettings,
    onUpdate: (LauncherSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SettingsBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsTopBar(
            title = "Home screen",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCardContainer {
            SectionHeader(title = "General")
            SettingToggleItem(
                title = "Lock layout",
                subtitle = "Icons and widgets can be added, removed and moved on the homescreen",
                checked = settings.lockLayout,
                onCheckedChange = { onUpdate(settings.copy(lockLayout = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Add app icons to home screen",
                subtitle = "For new apps",
                checked = settings.addIconsToHome,
                onCheckedChange = { onUpdate(settings.copy(addIconsToHome = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Double tap to sleep",
                subtitle = "Double tap on empty space for screen off",
                checked = settings.doubleTapToSleep,
                onCheckedChange = { onUpdate(settings.copy(doubleTapToSleep = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Wallpaper scrolling",
                subtitle = "Wallpaper scrolling effect for multiple screens",
                checked = settings.wallpaperScrolling,
                onCheckedChange = { onUpdate(settings.copy(wallpaperScrolling = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Wallpaper zooming",
                subtitle = "Zoom in or out the wallpaper when using drawer or recent apps",
                checked = settings.wallpaperZooming,
                onCheckedChange = { onUpdate(settings.copy(wallpaperZooming = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Allow short parallax",
                subtitle = "Enable full wallpaper scroll effect on smaller numbers of pages instead of cropping the wallpaper",
                checked = settings.allowShortParallax,
                onCheckedChange = { onUpdate(settings.copy(allowShortParallax = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Single page center",
                subtitle = "Center wallpaper if only using a single page",
                checked = settings.singlePageCenter,
                onCheckedChange = { onUpdate(settings.copy(singlePageCenter = it)) }
            )
        }

        SettingsCardContainer {
            SectionHeader(title = "Interface")
            SettingToggleItem(
                title = "Swipe to access Google app",
                subtitle = "When you swipe right from main home screen",
                checked = settings.swipeToGoogle,
                onCheckedChange = { onUpdate(settings.copy(swipeToGoogle = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Show status bar",
                subtitle = "Show/hide the status bar when viewing the home screen",
                checked = settings.showStatusBar,
                onCheckedChange = { onUpdate(settings.copy(showStatusBar = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Enable status bar shadow",
                subtitle = "Add a translucent shadow to the status bar",
                checked = settings.statusBarShadow,
                onCheckedChange = { onUpdate(settings.copy(statusBarShadow = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Icon labels on desktop",
                subtitle = "Show icon labels on desktop",
                checked = settings.iconLabelsOnDesktop,
                onCheckedChange = { onUpdate(settings.copy(iconLabelsOnDesktop = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Hotseat background",
                subtitle = "Add translucent background to the app dock",
                checked = settings.hotseatBackground,
                onCheckedChange = { onUpdate(settings.copy(hotseatBackground = it)) }
            )
            if (settings.hotseatBackground) {
                HorizontalDivider(color = Color(0xFFF3ECE7))
                SettingSliderItem(
                    title = "Background opacity",
                    value = settings.hotseatBackgroundOpacity,
                    valueRange = 10f..100f,
                    displayValue = "Value: ${settings.hotseatBackgroundOpacity} % (by default)",
                    onValueChange = { onUpdate(settings.copy(hotseatBackgroundOpacity = it)) }
                )
            }
        }

        SettingsCardContainer {
            SectionHeader(title = "Quickspace")
            SettingToggleItem(
                title = "At A Glance",
                subtitle = "Show at the top of your home screen",
                checked = settings.atAGlance,
                onCheckedChange = { onUpdate(settings.copy(atAGlance = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Random messages",
                subtitle = "Make your companion more lively with random messages",
                checked = settings.randomMessages,
                onCheckedChange = { onUpdate(settings.copy(randomMessages = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Extended style",
                subtitle = "Switch to extended style",
                checked = settings.extendedStyle,
                onCheckedChange = { onUpdate(settings.copy(extendedStyle = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Now playing",
                subtitle = "Show the song you're playing",
                checked = settings.nowPlaying,
                onCheckedChange = { onUpdate(settings.copy(nowPlaying = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Weather condition",
                subtitle = "Requires weather service to be enabled",
                checked = settings.weatherCondition,
                onCheckedChange = { onUpdate(settings.copy(weatherCondition = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Current City",
                subtitle = "Display current city",
                checked = settings.currentCity,
                onCheckedChange = { onUpdate(settings.copy(currentCity = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Detailed Weather condition",
                subtitle = "Display detailed weather condition and temperature",
                checked = settings.detailedWeather,
                onCheckedChange = { onUpdate(settings.copy(detailedWeather = it)) }
            )
        }

        SettingsCardContainer {
            SectionHeader(title = "Search bar")
            SettingToggleItem(
                title = "Google search bar",
                subtitle = "Search bar in the bottom dock",
                checked = settings.googleSearchBar,
                onCheckedChange = { onUpdate(settings.copy(googleSearchBar = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Music search",
                subtitle = "Start music search on pressing mic icon",
                checked = settings.musicSearch,
                onCheckedChange = { onUpdate(settings.copy(musicSearch = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Themed hotseat search bar",
                subtitle = "Use themed icons style for search bar",
                checked = settings.themedHotseatSearchBar,
                onCheckedChange = { onUpdate(settings.copy(themedHotseatSearchBar = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Monochrome search bar theme",
                subtitle = "Enable monochrome theme for search bar",
                checked = settings.monochromeSearchTheme,
                onCheckedChange = { onUpdate(settings.copy(monochromeSearchTheme = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingSliderItem(
                title = "Corner radius",
                value = settings.searchBarCornerRadius,
                valueRange = 0f..100f,
                displayValue = "Value: ${settings.searchBarCornerRadius} % (by default)",
                onValueChange = { onUpdate(settings.copy(searchBarCornerRadius = it)) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun GesturesSettingsScreen(
    settings: LauncherSettings,
    onUpdate: (LauncherSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGestureActionDialog by remember { mutableStateOf(false) }

    val gestureActions = listOf(
        0 to "None (Disabled)",
        1 to "Open App Drawer",
        2 to "Open Recents Overview",
        3 to "Open Launcher Settings",
        4 to "Quick Lock Screen",
        5 to "Open Wallpaper Picker"
    )

    val currentActionLabel = gestureActions.firstOrNull { it.first == settings.shakeGestureAction }?.second ?: "None (Disabled)"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SettingsBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsTopBar(
            title = "Gestures settings",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCardContainer {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_gesture_shake),
                    contentDescription = "Gesture shake illustration",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                )
            }

            SettingNavigationItem(
                title = "Shake gesture actions",
                subtitle = "Selected: $currentActionLabel",
                onClick = { showGestureActionDialog = true }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingSliderItem(
                title = "Shake gestures Intensity",
                value = settings.shakeGestureIntensity,
                valueRange = 1f..10f,
                displayValue = "Value: ${settings.shakeGestureIntensity} (Sensitivity)",
                onValueChange = { onUpdate(settings.copy(shakeGestureIntensity = it)) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showGestureActionDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showGestureActionDialog = false }) {
            androidx.compose.material3.Card(
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Shake Gesture Action",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF231F20)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    gestureActions.forEach { (actionId, label) ->
                        val isSelected = settings.shakeGestureAction == actionId
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onUpdate(settings.copy(shakeGestureAction = actionId))
                                    showGestureActionDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onUpdate(settings.copy(shakeGestureAction = actionId))
                                    showGestureActionDialog = false
                                },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = com.example.ui.components.CoralAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = Color(0xFF231F20)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.material3.TextButton(onClick = { showGestureActionDialog = false }) {
                            Text("Cancel", color = com.example.ui.components.CoralAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppDrawerSettingsScreen(
    settings: LauncherSettings,
    onUpdate: (LauncherSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SettingsBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsTopBar(
            title = "App drawer",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCardContainer {
            SettingToggleItem(
                title = "App search bar",
                subtitle = "Search bar on top of the app drawer",
                checked = settings.appSearchBar,
                onCheckedChange = { onUpdate(settings.copy(appSearchBar = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Icon labels in drawer",
                subtitle = "Show labels below icons in app drawer",
                checked = settings.iconLabelsInDrawer,
                onCheckedChange = { onUpdate(settings.copy(iconLabelsInDrawer = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingSliderItem(
                title = "Row height",
                value = settings.drawerRowHeight,
                valueRange = 50f..100f,
                displayValue = "Value: ${settings.drawerRowHeight} % ↺",
                onValueChange = { onUpdate(settings.copy(drawerRowHeight = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingSliderItem(
                title = "Background opacity",
                value = settings.drawerBackgroundOpacity,
                valueRange = 20f..100f,
                displayValue = "Value: ${settings.drawerBackgroundOpacity} % (by default)",
                onValueChange = { onUpdate(settings.copy(drawerBackgroundOpacity = it)) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RecentsSettingsScreen(
    settings: LauncherSettings,
    onUpdate: (LauncherSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SettingsBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsTopBar(
            title = "Recents",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCardContainer {
            SectionHeader(title = "General")
            SettingToggleItem(
                title = "Memory info",
                subtitle = "Show RAM consumption in overview",
                checked = settings.recentsMemoryInfo,
                onCheckedChange = { onUpdate(settings.copy(recentsMemoryInfo = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingSliderItem(
                title = "Background opacity",
                value = settings.recentsBackgroundOpacity,
                valueRange = 10f..100f,
                displayValue = "Value: ${settings.recentsBackgroundOpacity} % (by default)",
                onValueChange = { onUpdate(settings.copy(recentsBackgroundOpacity = it)) }
            )
        }

        SettingsCardContainer {
            SectionHeader(title = "Quick actions")
            SettingToggleItem(
                title = "Clear all",
                subtitle = "Clear all task overview card",
                checked = settings.recentsClearAll,
                onCheckedChange = { onUpdate(settings.copy(recentsClearAll = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingSliderItem(
                title = "Shake phone to clear recents intensity",
                value = settings.shakeToClearRecentsIntensity,
                valueRange = 1f..10f,
                displayValue = "Value: ${settings.shakeToClearRecentsIntensity} (by default)",
                onValueChange = { onUpdate(settings.copy(shakeToClearRecentsIntensity = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Kill app",
                subtitle = "Force kill selected background process",
                checked = settings.recentsKillApp,
                onCheckedChange = { onUpdate(settings.copy(recentsKillApp = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Lens",
                subtitle = "OCR and search contents on screen",
                checked = settings.recentsLens,
                onCheckedChange = { onUpdate(settings.copy(recentsLens = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Pin app",
                subtitle = "Keep task persistent in background",
                checked = settings.recentsPinApp,
                onCheckedChange = { onUpdate(settings.copy(recentsPinApp = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Screenshot",
                subtitle = "Capture recents thumbnail screenshot",
                checked = settings.recentsScreenshot,
                onCheckedChange = { onUpdate(settings.copy(recentsScreenshot = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Split app",
                subtitle = "Launch multitasking split screen mode",
                checked = settings.recentsSplitApp,
                onCheckedChange = { onUpdate(settings.copy(recentsSplitApp = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Shake phone to clear all tasks",
                subtitle = "Clear on shake motion",
                checked = settings.recentsShakeToClearAll,
                onCheckedChange = { onUpdate(settings.copy(recentsShakeToClearAll = it)) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MiscellaneousSettingsScreen(
    settings: LauncherSettings,
    allApps: List<AppItem> = emptyList(),
    onUpdate: (LauncherSettings) -> Unit,
    onRestartLauncher: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHiddenAppsDialog by remember { mutableStateOf(false) }
    var hiddenSearchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SettingsBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsTopBar(
            title = "Miscellaneous",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCardContainer {
            SettingToggleItem(
                title = "Use taskbar",
                subtitle = "For opening and switching apps anywhere",
                checked = settings.useTaskbar,
                onCheckedChange = { onUpdate(settings.copy(useTaskbar = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Allow home screen rotation",
                subtitle = "When phone is rotated",
                checked = settings.allowHomeScreenRotation,
                onCheckedChange = { onUpdate(settings.copy(allowHomeScreenRotation = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Enable haptic feedback on recents scroll",
                subtitle = null,
                checked = settings.enableHapticOnRecents,
                onCheckedChange = { onUpdate(settings.copy(enableHapticOnRecents = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingSliderItem(
                title = "Launcher vibration intensity",
                value = settings.launcherVibrationIntensity,
                valueRange = 0f..10f,
                displayValue = "Value: ${settings.launcherVibrationIntensity} (by default)",
                onValueChange = { onUpdate(settings.copy(launcherVibrationIntensity = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingNavigationItem(
                title = "Hidden & Protected apps",
                subtitle = if (settings.hiddenAppPackages.isEmpty()) "No apps hidden" else "${settings.hiddenAppPackages.size} apps hidden",
                onClick = { showHiddenAppsDialog = true }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingToggleItem(
                title = "Action Toasts",
                subtitle = "Enable-disable Toast clear all",
                checked = settings.actionToasts,
                onCheckedChange = { onUpdate(settings.copy(actionToasts = it)) }
            )
            HorizontalDivider(color = Color(0xFFF3ECE7))
            SettingNavigationItem(
                title = "Restart",
                subtitle = "Restart the launcher manually to apply pending settings",
                onClick = onRestartLauncher
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showHiddenAppsDialog) {
        val filtered = remember(hiddenSearchQuery, allApps) {
            if (hiddenSearchQuery.isBlank()) allApps
            else allApps.filter { it.label.contains(hiddenSearchQuery, ignoreCase = true) }
        }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showHiddenAppsDialog = false }) {
            androidx.compose.material3.Card(
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .heightIn(max = 540.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Hidden & Protected Apps",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF231F20)
                    )
                    Text(
                        text = "Selected apps will be hidden from the App Drawer",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF5F0EB),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = hiddenSearchQuery,
                                onValueChange = { hiddenSearchQuery = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color(0xFF231F20)),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(com.example.ui.components.CoralAccent),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                    ) {
                        items(filtered, key = { it.packageName }) { app ->
                            val isHidden = settings.hiddenAppPackages.contains(app.packageName)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val newSet = if (isHidden) {
                                            settings.hiddenAppPackages - app.packageName
                                        } else {
                                            settings.hiddenAppPackages + app.packageName
                                        }
                                        onUpdate(settings.copy(hiddenAppPackages = newSet))
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                            ) {
                                AppIconBadge(app = app, sizeDp = 36.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = app.label,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF231F20),
                                    modifier = Modifier.weight(1f)
                                )
                                androidx.compose.material3.Checkbox(
                                    checked = isHidden,
                                    onCheckedChange = { checked ->
                                        val newSet = if (checked) {
                                            settings.hiddenAppPackages + app.packageName
                                        } else {
                                            settings.hiddenAppPackages - app.packageName
                                        }
                                        onUpdate(settings.copy(hiddenAppPackages = newSet))
                                    },
                                    colors = androidx.compose.material3.CheckboxDefaults.colors(
                                        checkedColor = com.example.ui.components.CoralAccent
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.material3.TextButton(onClick = { showHiddenAppsDialog = false }) {
                            Text("Done", color = com.example.ui.components.CoralAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
