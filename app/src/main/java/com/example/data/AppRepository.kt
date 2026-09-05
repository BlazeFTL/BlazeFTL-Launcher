package com.example.data

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.Locale
import com.example.util.IconUtils
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Widgets
import com.example.model.AppItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    @Volatile
    private var cachedInstalledApps: List<AppItem>? = null

    private val _installedAppsFlow = MutableStateFlow<List<AppItem>>(emptyList())
    val installedAppsFlow: StateFlow<List<AppItem>> = _installedAppsFlow.asStateFlow()

    init {
        _installedAppsFlow.value = getPreloadedApps()
    }

    fun buildInstalledAppDataAsync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val loaded = getInstalledApps()
            _installedAppsFlow.value = loaded
        }
    }

    fun getPreloadedApps(): List<AppItem> {
        cachedInstalledApps?.let { return it }
        val apps = getCuratedApps().distinctBy { it.uniqueKey }.sortedBy { it.label.lowercase() }
        cachedInstalledApps = apps
        return apps
    }

    suspend fun getInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            packageManager.queryIntentActivities(mainIntent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        val chunks = resolveInfos.chunked(16)
        val deviceApps = chunks.map { chunk ->
            async(Dispatchers.IO) {
                chunk.mapNotNull { resolveInfo ->
                    try {
                        val packageName = resolveInfo.activityInfo.packageName
                        if (packageName == context.packageName) return@mapNotNull null

                        val label = resolveInfo.loadLabel(packageManager).toString()
                        val iconDrawable = resolveInfo.loadIcon(packageManager)
                        val iconBitmap = IconUtils.drawableToImageBitmap(iconDrawable, packageName)
                        AppItem(
                            packageName = packageName,
                            activityName = resolveInfo.activityInfo.name,
                            label = label,
                            iconDrawable = iconDrawable,
                            iconBitmap = iconBitmap,
                            isSystemApp = false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }.awaitAll().flatten().distinctBy { it.uniqueKey }
        .sortedBy { it.label.lowercase() }

        val result = if (deviceApps.isNotEmpty()) {
            deviceApps
        } else {
            // Curated 153 apps matching phone screenshot with pre-built cached icons
            getCuratedApps().distinctBy { it.uniqueKey }.sortedBy { it.label.lowercase() }
        }
        cachedInstalledApps = result
        _installedAppsFlow.value = result
        result
    }

    fun getDockApps(): List<AppItem> {
        val pm = context.packageManager
        val dockList = mutableListOf<AppItem>()

        // 1. Phone / Dialer
        val phoneIntent = Intent(Intent.ACTION_DIAL)
        val phoneResolve = try { pm.resolveActivity(phoneIntent, PackageManager.MATCH_DEFAULT_ONLY) } catch (e: Exception) { null }
        if (phoneResolve != null && phoneResolve.activityInfo.packageName != "android" && phoneResolve.activityInfo.packageName != context.packageName) {
            try {
                val pName = phoneResolve.activityInfo.packageName
                val d = phoneResolve.loadIcon(pm)
                val bmp = IconUtils.drawableToImageBitmap(d, pName)
                dockList.add(
                    AppItem(
                        packageName = pName,
                        activityName = phoneResolve.activityInfo.name,
                        label = phoneResolve.loadLabel(pm).toString(),
                        iconDrawable = d,
                        iconBitmap = bmp,
                        iconVector = Icons.Default.Phone,
                        iconColor = 0xFF2196F3
                    )
                )
            } catch (e: Exception) {
                // Ignore
            }
        }

        // 2. Messages / SMS
        val msgIntent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("smsto:") }
        val msgResolve = try { pm.resolveActivity(msgIntent, PackageManager.MATCH_DEFAULT_ONLY) } catch (e: Exception) { null }
        if (msgResolve != null && msgResolve.activityInfo.packageName != "android" && msgResolve.activityInfo.packageName != context.packageName) {
            try {
                val pName = msgResolve.activityInfo.packageName
                val d = msgResolve.loadIcon(pm)
                val bmp = IconUtils.drawableToImageBitmap(d, pName)
                dockList.add(
                    AppItem(
                        packageName = pName,
                        activityName = msgResolve.activityInfo.name,
                        label = msgResolve.loadLabel(pm).toString(),
                        iconDrawable = d,
                        iconBitmap = bmp,
                        iconVector = Icons.Outlined.Chat,
                        iconColor = 0xFF4CAF50
                    )
                )
            } catch (e: Exception) {
                // Ignore
            }
        }

        // 3. Gallery / Photos - ONLY if found and known on device
        val galleryIntent = Intent(Intent.ACTION_VIEW).apply { type = "image/*" }
        val galleryResolve = try { pm.resolveActivity(galleryIntent, 0) } catch (e: Exception) { null }
        if (galleryResolve != null && galleryResolve.activityInfo.packageName != "android" && galleryResolve.activityInfo.packageName != context.packageName) {
            val pName = galleryResolve.activityInfo.packageName
            val launchIntent = try { pm.getLaunchIntentForPackage(pName) } catch (e: Exception) { null }
            if (launchIntent != null) {
                try {
                    val d = galleryResolve.loadIcon(pm)
                    val bmp = IconUtils.drawableToImageBitmap(d, pName)
                    dockList.add(
                        AppItem(
                            packageName = pName,
                            activityName = galleryResolve.activityInfo.name,
                            label = galleryResolve.loadLabel(pm).toString(),
                            iconDrawable = d,
                            iconBitmap = bmp,
                            iconVector = Icons.Outlined.Image,
                            iconColor = 0xFF26A69A
                        )
                    )
                } catch (e: Exception) {
                    // Do not add unknown gallery
                }
            }
        }

        // 4. Browser / Chrome
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
        val webResolve = try { pm.resolveActivity(webIntent, PackageManager.MATCH_DEFAULT_ONLY) } catch (e: Exception) { null }
        if (webResolve != null && webResolve.activityInfo.packageName != "android" && webResolve.activityInfo.packageName != context.packageName) {
            try {
                val pName = webResolve.activityInfo.packageName
                val d = webResolve.loadIcon(pm)
                val bmp = IconUtils.drawableToImageBitmap(d, pName)
                dockList.add(
                    AppItem(
                        packageName = pName,
                        activityName = webResolve.activityInfo.name,
                        label = webResolve.loadLabel(pm).toString(),
                        iconDrawable = d,
                        iconBitmap = bmp,
                        iconVector = Icons.Default.Language,
                        iconColor = 0xFFFF7043
                    )
                )
            } catch (e: Exception) {
                // Ignore
            }
        }

        // 5. Camera
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        val cameraResolve = try { pm.resolveActivity(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY) } catch (e: Exception) { null }
        if (cameraResolve != null && cameraResolve.activityInfo.packageName != "android" && cameraResolve.activityInfo.packageName != context.packageName) {
            try {
                val pName = cameraResolve.activityInfo.packageName
                val d = cameraResolve.loadIcon(pm)
                val bmp = IconUtils.drawableToImageBitmap(d, pName)
                dockList.add(
                    AppItem(
                        packageName = pName,
                        activityName = cameraResolve.activityInfo.name,
                        label = cameraResolve.loadLabel(pm).toString(),
                        iconDrawable = d,
                        iconBitmap = bmp,
                        iconVector = Icons.Default.CameraAlt,
                        iconColor = 0xFFE91E63
                    )
                )
            } catch (e: Exception) {
                // Ignore
            }
        }

        // If in preview with no resolved apps, fallback to basic curated dock apps without duplicates
        if (dockList.isEmpty()) {
            dockList.addAll(
                listOf(
                    AppItem("com.android.dialer", label = "Phone", iconVector = Icons.Default.Phone, iconColor = 0xFF2196F3),
                    AppItem("com.android.messaging", label = "Messages", iconVector = Icons.Outlined.Chat, iconColor = 0xFF4CAF50),
                    AppItem("com.android.chrome", label = "Browser", iconVector = Icons.Default.Language, iconColor = 0xFFFF7043),
                    AppItem("com.android.camera2", label = "Camera", iconVector = Icons.Default.CameraAlt, iconColor = 0xFFE91E63)
                )
            )
        }

        return dockList.distinctBy { it.packageName }
    }

    fun getHomeScreenApps(): List<AppItem> {
        // Clean and empty desktop by default like Nova Launcher, letting the user customize and pin apps as they wish
        return emptyList()
    }

    private fun getCuratedApps(): List<AppItem> {
        val appDefs = listOf(
            Triple("com.cloudflare.onedotonedotonedotone", "1.1.1.1", 0xFF4285F4),
            Triple("com.desigk", "1Desigk", 0xFF009688),
            Triple("com.ab.direct", "AB Direct", 0xFFE91E63),
            Triple("com.adclose", "AdClose", 0xFF3F51B5),
            Triple("com.adsregex", "Ads Regex+", 0xFF607D8B),
            Triple("com.aihub", "AI Hub", 0xFF673AB7),
            Triple("com.alljobs", "Alljobs by Tel...", 0xFF4CAF50),
            Triple("com.apkeditor.pro", "APK Editor", 0xFFFF9800),
            Triple("com.apktool", "Apktool M", 0xFF03A9F4),
            Triple("com.applist", "App List Bac...", 0xFFE53935),
            Triple("com.audiofx", "Audio effects", 0xFF1E88E5),
            Triple("com.aurora.store", "Aurora Store", 0xFF3949AB),
            Triple("com.google.authenticator", "Authenticator", 0xFF00ACC1),
            Triple("org.ayugram", "AyuGram", 0xFF8E24AA),
            Triple("com.bangla.dict", "Bangla Dicti...", 0xFF1565C0),
            Triple("com.bdalljob", "BdAllJob", 0xFF43A047),
            Triple("com.bdjobs", "Bdjobs", 0xFF2E7D32),
            Triple("com.bkash", "bKash", 0xFFE91E63),
            Triple("com.byebyedpi", "ByeByeDPI", 0xFF0288D1),
            Triple("com.bypassempire", "Bypass Empire", 0xFF795548),
            Triple("com.android.calculator2", "Calculator", 0xFF37474F),
            Triple("com.google.android.calendar", "Calendar", 0xFFD32F2F),
            Triple("com.google.android.calendar.work", "Calendar (Work)", 0xFF1976D2),
            Triple("com.callvolume.booster", "Call Volume B...", 0xFF00897B),
            Triple("com.android.camera2", "Camera", 0xFF263238),
            Triple("com.openai.chatgpt", "ChatGPT", 0xFF10A37F),
            Triple("com.google.android.apps.classroom", "Classroom", 0xFF2E7D32),
            Triple("com.anthropic.claude", "Claude", 0xFFD97706),
            Triple("com.anthropic.claude.beta", "Claude Beta", 0xFFF59E0B),
            Triple("com.anthropic.claude.work", "Claude Work", 0xFFB45309),
            Triple("com.android.deskclock", "Clock", 0xFF0288D1),
            Triple("com.comparekit", "CompareKit", 0xFF5C6BC0),
            Triple("com.google.android.contacts", "Contacts", 0xFF1976D2),
            Triple("com.cpuz", "CPU-Z", 0xFF455A64),
            Triple("com.daraz.android", "Daraz", 0xFFFF5722),
            Triple("com.deepseek.chat", "DeepSeek", 0xFF3B82F6),
            Triple("com.devtools", "Dev Tools", 0xFF009688),
            Triple("com.devassistant", "Developer Ass...", 0xFF00ACC1),
            Triple("com.discord", "Discord", 0xFF5865F2),
            Triple("com.dnsnet", "DNSNet", 0xFF3F51B5),
            Triple("com.docscanner", "Doc Scanner", 0xFF00897B),
            Triple("com.domainchecker", "Domain Chec...", 0xFF26A69A),
            Triple("com.google.android.apps.docs", "Drive", 0xFF0F9D58),
            Triple("com.earthquake", "Earthquake", 0xFFE64A19),
            Triple("com.earthquake.network", "Earthquake N...", 0xFFD84315),
            Triple("com.emptyfolder.cleaner", "Empty Folder ...", 0xFF546E7A),
            Triple("com.errorx", "ErrorX", 0xFFC2185B),
            Triple("com.estrongs.android.pop", "ES File Explorer", 0xFF1976D2),
            Triple("com.facebook.katana", "Facebook", 0xFF1877F2),
            Triple("com.google.android.apps.nbu.files", "Files", 0xFF4285F4),
            Triple("com.findhub", "Find Hub", 0xFF00838F),
            Triple("org.mozilla.firefox", "Firefox", 0xFFFF5722),
            Triple("org.mozilla.fenix", "Firefox Nightly", 0xFFE65100),
            Triple("com.ficlash", "FIClash", 0xFF5E35B1),
            Triple("com.android.fmradio", "FM Radio", 0xFFE91E63),
            Triple("com.folderuntangler", "Folder Untang...", 0xFF6D4C41),
            Triple("com.foodpanda", "foodpanda", 0xFFD81B60),
            Triple("com.google.android.gallery", "Gallery", 0xFF43A047),
            Triple("com.gallery.private", "Gallery Private", 0xFF1B5E20),
            Triple("com.game.space", "Game", 0xFF7C4DFF),
            Triple("com.game.space.pro", "Game Space", 0xFF651FFF),
            Triple("com.google.android.inputmethod.latin", "Gboard", 0xFF1E88E5),
            Triple("com.google.android.apps.bard", "Gemini", 0xFF4285F4),
            Triple("com.gitpilot", "GitPilot", 0xFF24292E),
            Triple("com.google.android.gm", "Gmail", 0xFFEA4335),
            Triple("com.google.android.googlequicksearchbox", "Google", 0xFF4285F4),
            Triple("com.oasisfeng.greenify", "Greenify", 0xFF4CAF50),
            Triple("idm.internet.download.manager", "IDM+", 0xFF0288D1),
            Triple("com.pikpok.dr2.play", "Into the Dead", 0xFF212121),
            Triple("com.jobcircular", "Job Circular", 0xFF2E7D32),
            Triple("com.jobkar", "Jobkar", 0xFF388E3C),
            Triple("com.kimi.moonshot", "Kimi", 0xFF673AB7),
            Triple("com.ksuwebui", "KsuWebUI", 0xFF00897B),
            Triple("com.lastchat", "LastChat", 0xFF8E24AA),
            Triple("com.lemur.browser", "Lemur Browser", 0xFF00ACC1),
            Triple("com.microsoft.appmanager", "Link to Windo...", 0xFF0078D4),
            Triple("com.linkedin.android", "LinkedIn", 0xFF0A66C2),
            Triple("com.facebook.lite", "Lite", 0xFF1877F2),
            Triple("com.google.android.apps.maps", "Maps", 0xFF34A853),
            Triple("org.matrix", "Matrix", 0xFF0DBD8B),
            Triple("com.facebook.orca", "Messenger", 0xFF0084FF),
            Triple("com.xiaomi.wearable", "Mi Fitness", 0xFFFF6900),
            Triple("com.microsoft.office.officehubrow", "Microsoft 365", 0xFFD83B01),
            Triple("com.mixplorer", "Mixplorer", 0xFF37474F),
            Triple("com.flyersoft.moonreader", "Moon Reader", 0xFF5D4037),
            Triple("com.mxtech.videoplayer.ad", "MX Player", 0xFF0084FF),
            Triple("com.netflix.mediaclient", "Netflix", 0xFFE50914),
            Triple("notion.id", "Notion", 0xFF000000),
            Triple("com.teslacoilsw.launcher", "Nova Launcher", 0xFFFF5722),
            Triple("md.obsidian", "Obsidian", 0xFF7C3AED),
            Triple("com.microsoft.skydrive", "OneDrive", 0xFF0078D4),
            Triple("net.sourceforge.opencamera", "Open Camera", 0xFF009688),
            Triple("com.opera.browser", "Opera", 0xFFFF1B2D),
            Triple("com.microsoft.office.outlook", "Outlook", 0xFF0078D4),
            Triple("com.paypal.android.p2pmobile", "PayPal", 0xFF003087),
            Triple("com.google.android.dialer", "Phone", 0xFF1E88E5),
            Triple("com.google.android.apps.photos", "Photos", 0xFFEA4335),
            Triple("com.pinterest", "Pinterest", 0xFFE60023),
            Triple("com.pixelstudio", "Pixel Studio", 0xFF9C27B0),
            Triple("com.android.vending", "Play Store", 0xFF0086F8),
            Triple("com.ideashower.readitlater.pro", "Pocket", 0xFFEF4056),
            Triple("com.maxmpz.audioplayer", "Poweramp", 0xFF1976D2),
            Triple("ch.protonmail.android", "Proton Mail", 0xFF6D4AFF),
            Triple("ch.protonvpn.android", "Proton VPN", 0xFF6D4AFF),
            Triple("com.sika524.android.quickshortcut", "QuickShortcut", 0xFF00ACC1),
            Triple("com.reddit.frontpage", "Reddit", 0xFFFF4500),
            Triple("app.revanced.manager.flutter", "ReVanced", 0xFF3B82F6),
            Triple("com.sec.android.app.shealth", "Samsung Health", 0xFF2962FF),
            Triple("com.android.settings", "Settings", 0xFF546E7A),
            Triple("com.shazam.android", "Shazam", 0xFF0088FF),
            Triple("org.thoughtcrime.securesms", "Signal", 0xFF3A76F0),
            Triple("com.Slack", "Slack", 0xFF4A154B),
            Triple("com.smartaudio", "Smart Audio", 0xFFE91E63),
            Triple("com.snapchat.android", "Snapchat", 0xFFFFFC00),
            Triple("com.niksoftware.snapseed", "Snapseed", 0xFF4CAF50),
            Triple("com.soundhound.android", "SoundHound", 0xFFFF7043),
            Triple("com.spotify.music", "Spotify", 0xFF1DB954),
            Triple("com.valvesoftware.android.steam.community", "Steam", 0xFF171A21),
            Triple("com.strava", "Strava", 0xFFFC4C02),
            Triple("com.touchtype.swiftkey", "SwiftKey", 0xFF00897B),
            Triple("org.telegram.messenger", "Telegram", 0xFF24A1DE),
            Triple("com.termux", "Termux", 0xFF000000),
            Triple("com.zhiliaoapp.musically", "TikTok", 0xFF000000),
            Triple("org.torproject.torbrowser", "Tor Browser", 0xFF7D4698),
            Triple("com.truecaller", "Truecaller", 0xFF0088FF),
            Triple("tv.twitch.android.app", "Twitch", 0xFF9146FF),
            Triple("com.twitter.android", "Twitter / X", 0xFF000000),
            Triple("com.ubercab", "Uber", 0xFF000000),
            Triple("org.videolan.vlc", "VLC", 0xFFFF8800),
            Triple("com.vivaldi.browser", "Vivaldi", 0xFFEF3939),
            Triple("com.vscode.web", "VS Code Web", 0xFF007ACC),
            Triple("com.waze", "Waze", 0xFF33CCFF),
            Triple("com.weather.forecast", "Weather", 0xFF0288D1),
            Triple("com.whatsapp", "WhatsApp", 0xFF25D366),
            Triple("org.wikipedia", "Wikipedia", 0xFF212121),
            Triple("com.wireguard.android", "WireGuard", 0xFF88171A),
            Triple("de.robv.android.xposed.installer", "Xposed", 0xFFE65100),
            Triple("com.google.android.youtube", "YouTube", 0xFFFF0000),
            Triple("com.google.android.apps.youtube.music", "YouTube Music", 0xFFFF0000),
            Triple("us.zoom.videomeetings", "Zoom", 0xFF2D8CFF),
            Triple("ru.zdevs.zarchiver", "ZArchiver", 0xFF43A047),
            Triple("com.cloudflare.warp", "1.1.1.1 Warp+", 0xFFF4511E),
            Triple("com.bangla.keyboard", "Ridmik Keyboard", 0xFF00897B),
            Triple("com.speedtest", "Speedtest", 0xFF141526),
            Triple("com.google.android.keep", "Keep Notes", 0xFFF4B400),
            Triple("com.adobe.reader", "Adobe Acrobat", 0xFFD32F2F),
            Triple("com.duolingo", "Duolingo", 0xFF58CC02),
            Triple("com.soundcloud.android", "SoundCloud", 0xFFFF5500),
            Triple("com.pinterest.tappit", "Shuffles", 0xFFE60023),
            Triple("com.supercell.clashofclans", "Clash of Clans", 0xFFFF9800),
            Triple("com.microsoft.teams", "Microsoft Teams", 0xFF464EB8),
            Triple("com.chess", "Chess.com", 0xFF7FA650),
            Triple("com.binance.dev", "Binance", 0xFFF0B90B),
            Triple("com.coinbase.android", "Coinbase", 0xFF0052FF),
            Triple("com.adobe.lrmobile", "Lightroom", 0xFF001E36),
            Triple("com.canva.editor", "Canva", 0xFF7D2AE8),
            Triple("com.figma.mirror", "Figma", 0xFFF24E1E),
            Triple("com.github.android", "GitHub", 0xFF24292E),
            Triple("com.vsco.cam", "VSCO", 0xFF000000),
            Triple("com.trello", "Trello", 0xFF0079BF),
            Triple("com.todoist", "Todoist", 0xFFE44332),
            Triple("com.ticktick.task", "TickTick", 0xFF4A90E2),
            Triple("com.asus.calculator", "Unit Converter", 0xFF00838F),
            Triple("com.google.android.apps.translate", "Translate", 0xFF4285F4),
            Triple("com.plexapp.android", "Plex", 0xFFE5A00D),
            Triple("com.pocketcasts.android", "Pocket Casts", 0xFFF43E37)
        )

        return appDefs.map { (pkg, label, color) ->
            makeCuratedApp(pkg, label, color)
        }
    }

    private fun makeCuratedApp(pkg: String, label: String, color: Long): AppItem {
        return AppItem(
            packageName = pkg,
            label = label,
            iconColor = color,
            iconBitmap = IconUtils.getFallbackAppIcon(label, color)
        )
    }

    fun launchApp(packageName: String, activityName: String? = null): Boolean {
        return try {
            if (packageName == "com.android.settings") {
                val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return true
            }

            val intent = if (!activityName.isNullOrBlank()) {
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = ComponentName(packageName, activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
            } else {
                context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
            }
            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            try {
                val fallbackIntent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (fallbackIntent != null) {
                    context.startActivity(fallbackIntent)
                    true
                } else {
                    false
                }
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun openAppInfo(packageName: String): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun uninstallApp(packageName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openWallpaperPicker(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SET_WALLPAPER).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getSystemMemoryInfo(): Pair<Long, Long> {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (actManager != null) {
                val memInfo = ActivityManager.MemoryInfo()
                actManager.getMemoryInfo(memInfo)
                val availMb = memInfo.availMem / (1024 * 1024)
                val totalMb = memInfo.totalMem / (1024 * 1024)
                Pair(availMb, totalMb)
            } else {
                Pair(3840L, 8192L)
            }
        } catch (e: Exception) {
            Pair(3840L, 8192L)
        }
    }

    fun getFormattedMemoryInfo(): String {
        val (availMb, totalMb) = getSystemMemoryInfo()
        val availGb = String.format(Locale.US, "%.1f", availMb / 1024f)
        val totalGb = String.format(Locale.US, "%.1f", totalMb / 1024f)
        return "RAM: $availGb GB / $totalGb GB available"
    }

    fun killBackgroundProcesses(packageName: String) {
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            actManager?.killBackgroundProcesses(packageName)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun isAudioPlaying(): Boolean {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            audioManager?.isMusicActive == true
        } catch (e: Exception) {
            false
        }
    }

    fun expandQuickSettings(): Boolean {
        return try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
            true
        } catch (e: Exception) {
            try {
                val statusBarService = context.getSystemService("statusbar")
                val statusBarManager = Class.forName("android.app.StatusBarManager")
                val method = statusBarManager.getMethod("expandSettingsPanel")
                method.invoke(statusBarService)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun triggerHapticFeedback(intensity: Int = 1) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                val duration = (intensity * 25L).coerceIn(20L, 100L)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            // Ignored
        }
    }
}
