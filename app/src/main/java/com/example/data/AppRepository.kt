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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

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

        val deviceApps = resolveInfos.mapNotNull { resolveInfo ->
            try {
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == context.packageName) return@mapNotNull null

                val label = resolveInfo.loadLabel(packageManager).toString()
                val iconDrawable = resolveInfo.loadIcon(packageManager)
                AppItem(
                    packageName = packageName,
                    activityName = resolveInfo.activityInfo.name,
                    label = label,
                    iconDrawable = iconDrawable,
                    isSystemApp = false
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.label.lowercase() }

        if (deviceApps.size >= 8) {
            deviceApps
        } else {
            // Combine with default curated launcher apps matching the reference screenshots
            val defaultApps = getCuratedApps()
            val combined = (deviceApps + defaultApps).distinctBy { it.label }
            combined.sortedBy { it.label.lowercase() }
        }
    }

    fun getDockApps(): List<AppItem> {
        return listOf(
            AppItem(
                packageName = "com.google.android.dialer",
                label = "Phone",
                iconVector = Icons.Default.Phone,
                iconColor = 0xFF2196F3
            ),
            AppItem(
                packageName = "com.google.android.apps.messaging",
                label = "Messages",
                iconVector = Icons.Outlined.Chat,
                iconColor = 0xFF4CAF50
            ),
            AppItem(
                packageName = "com.google.android.apps.photos",
                label = "Gallery",
                iconVector = Icons.Outlined.Image,
                iconColor = 0xFF26A69A
            ),
            AppItem(
                packageName = "org.mozilla.firefox",
                label = "Firefox",
                iconVector = Icons.Default.Language,
                iconColor = 0xFFFF7043
            ),
            AppItem(
                packageName = "org.mozilla.fenix",
                label = "Nightly",
                iconVector = Icons.Outlined.Public,
                iconColor = 0xFFAB47BC
            ),
            AppItem(
                packageName = "com.google.android.GoogleCamera",
                label = "Camera",
                iconVector = Icons.Default.CameraAlt,
                iconColor = 0xFF455A64
            )
        )
    }

    fun getHomeScreenApps(): List<AppItem> {
        return listOf(
            AppItem(packageName = "com.idmplus", label = "IDM+", iconVector = Icons.Outlined.Download, iconColor = 0xFFFF9800),
            AppItem(packageName = "com.mxtech.videoplayer.pro", label = "MX Player Pro", iconVector = Icons.Default.PlayArrow, iconColor = 0xFF2196F3),
            AppItem(packageName = "com.bdix.bypass", label = "Bdix ByPass", iconVector = Icons.Outlined.Widgets, iconColor = 0xFF00BCD4),
            AppItem(packageName = "com.facebook.lite", label = "Lite", iconVector = Icons.Outlined.Public, iconColor = 0xFF1877F2),
            AppItem(packageName = "com.oasisfeng.greenify", label = "Greenify", iconVector = Icons.Outlined.Tune, iconColor = 0xFF4CAF50),
            AppItem(packageName = "com.google.android.apps.maps", label = "Maps", iconVector = Icons.Default.Map, iconColor = 0xFFE91E63),
            AppItem(packageName = "com.facebook.orca", label = "Messenger", iconVector = Icons.Outlined.Chat, iconColor = 0xFF0084FF),
            AppItem(packageName = "org.telegram.ayugram", label = "AyuGram", iconVector = Icons.Outlined.Chat, iconColor = 0xFF9C27B0),
            AppItem(packageName = "com.lemur.browser", label = "Lemur Brow...", iconVector = Icons.Default.Language, iconColor = 0xFF00ACC1),
            AppItem(packageName = "com.google.android.apps.photos", label = "Gallery", iconVector = Icons.Outlined.Image, iconColor = 0xFF42A5F5),
            AppItem(packageName = "com.spark.aislop", label = "Ai Slop", iconVector = Icons.Default.SmartToy, iconColor = 0xFF7E57C2),
            AppItem(packageName = "com.xodo.pdf", label = "Xodo", iconVector = Icons.Outlined.Folder, iconColor = 0xFF26C6DA),
            AppItem(packageName = "com.google.android.youtube", label = "YouTube", iconVector = Icons.Default.VideoLibrary, iconColor = 0xFFF44336),
            AppItem(packageName = "com.myrobi.app", label = "My Robi", iconVector = Icons.Outlined.ShoppingBag, iconColor = 0xFFFF5722),
            AppItem(packageName = "com.android.vending", label = "Play Store", iconVector = Icons.Default.PlayArrow, iconColor = 0xFF4CAF50),
            AppItem(packageName = "com.android.settings", label = "Settings", iconVector = Icons.Default.Settings, iconColor = 0xFF607D8B)
        )
    }

    private fun getCuratedApps(): List<AppItem> {
        return listOf(
            AppItem("com.cloudflare.onedotonedotonedotone", label = "1.1.1.1", iconVector = Icons.Outlined.Lock, iconColor = 0xFF4285F4),
            AppItem("com.desigk", label = "1Desigk", iconVector = Icons.Outlined.Tune, iconColor = 0xFF009688),
            AppItem("com.ab.direct", label = "AB Direct", iconVector = Icons.Outlined.Widgets, iconColor = 0xFFE91E63),
            AppItem("com.adclose", label = "AdClose", iconVector = Icons.Outlined.Lock, iconColor = 0xFF3F51B5),
            AppItem("com.aihub", label = "AI Hub", iconVector = Icons.Default.SmartToy, iconColor = 0xFF673AB7),
            AppItem("com.alljobs", label = "Alljobs", iconVector = Icons.Outlined.Public, iconColor = 0xFF4CAF50),
            AppItem("com.apktool", label = "Apktool M", iconVector = Icons.Outlined.Terminal, iconColor = 0xFF03A9F4),
            AppItem("com.applist", label = "App List Bac...", iconVector = Icons.Outlined.Folder, iconColor = 0xFFE53935),
            AppItem("com.audiofx", label = "Audio effects", iconVector = Icons.Default.MusicNote, iconColor = 0xFF1E88E5),
            AppItem("com.aurora.store", label = "Aurora Store", iconVector = Icons.Outlined.ShoppingBag, iconColor = 0xFF3949AB),
            AppItem("com.google.authenticator", label = "Authenticator", iconVector = Icons.Outlined.Lock, iconColor = 0xFF00ACC1),
            AppItem("org.ayugram", label = "AyuGram", iconVector = Icons.Outlined.Chat, iconColor = 0xFF8E24AA),
            AppItem("com.bangla.dict", label = "Bangla Dicti...", iconVector = Icons.Outlined.Folder, iconColor = 0xFF1565C0),
            AppItem("com.bdalljob", label = "BdAllJob", iconVector = Icons.Outlined.Public, iconColor = 0xFF43A047),
            AppItem("com.bkash", label = "bKash", iconVector = Icons.Outlined.ShoppingBag, iconColor = 0xFFE91E63),
            AppItem("com.byebyedpi", label = "ByeByeDPI", iconVector = Icons.Outlined.Lock, iconColor = 0xFF0288D1),
            AppItem("com.android.calculator2", label = "Calculator", iconVector = Icons.Default.Calculate, iconColor = 0xFF37474F),
            AppItem("com.google.android.calendar", label = "Calendar", iconVector = Icons.Outlined.Widgets, iconColor = 0xFFD32F2F),
            AppItem("com.android.camera2", label = "Camera", iconVector = Icons.Default.CameraAlt, iconColor = 0xFF263238),
            AppItem("com.openai.chatgpt", label = "ChatGPT", iconVector = Icons.Default.SmartToy, iconColor = 0xFF10A37F),
            AppItem("com.anthropic.claude", label = "Claude", iconVector = Icons.Default.SmartToy, iconColor = 0xFFD97706),
            AppItem("com.android.deskclock", label = "Clock", iconVector = Icons.Outlined.Tune, iconColor = 0xFF0288D1),
            AppItem("com.google.android.contacts", label = "Contacts", iconVector = Icons.Outlined.AccountCircle, iconColor = 0xFF1976D2),
            AppItem("com.deepseek.chat", label = "DeepSeek", iconVector = Icons.Default.SmartToy, iconColor = 0xFF3B82F6),
            AppItem("com.discord", label = "Discord", iconVector = Icons.Outlined.Chat, iconColor = 0xFF5865F2),
            AppItem("com.google.android.apps.docs", label = "Drive", iconVector = Icons.Outlined.Folder, iconColor = 0xFF0F9D58),
            AppItem("org.mozilla.firefox", label = "Firefox", iconVector = Icons.Default.Language, iconColor = 0xFFFF5722),
            AppItem("com.google.android.gm", label = "Gmail", iconVector = Icons.Default.Email, iconColor = 0xFFEA4335),
            AppItem("com.google.android.googlequicksearchbox", label = "Google", iconVector = Icons.Outlined.Public, iconColor = 0xFF4285F4),
            AppItem("com.whatsapp", label = "WhatsApp", iconVector = Icons.Outlined.Chat, iconColor = 0xFF25D366),
            AppItem("com.google.android.youtube", label = "YouTube", iconVector = Icons.Default.VideoLibrary, iconColor = 0xFFFF0000),
            AppItem("com.android.settings", label = "Settings", iconVector = Icons.Default.Settings, iconColor = 0xFF546E7A)
        )
    }

    fun launchApp(packageName: String, activityName: String? = null): Boolean {
        return try {
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
            false
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
