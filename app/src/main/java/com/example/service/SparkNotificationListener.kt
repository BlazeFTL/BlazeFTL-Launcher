package com.example.service

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.model.NowPlayingTrack

class SparkNotificationListener : NotificationListenerService() {

    companion object {
        @Volatile
        var isConnected: Boolean = false
            private set

        fun getActivePlayingTrack(context: Context): NowPlayingTrack {
            try {
                val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
                    ?: return NowPlayingTrack()
                val component = ComponentName(context, SparkNotificationListener::class.java)
                val controllers = sessionManager.getActiveSessions(component)
                for (controller in controllers) {
                    val playbackState = controller.playbackState
                    if (playbackState != null && playbackState.state == PlaybackState.STATE_PLAYING) {
                        val metadata = controller.metadata
                        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                            ?: ""
                        if (!title.isNullOrBlank()) {
                            return NowPlayingTrack(
                                isPlaying = true,
                                title = title,
                                artist = artist,
                                packageName = controller.packageName
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                // Notification listener permission not granted or service not active yet
            } catch (e: Exception) {
                // Ignore unexpected exceptions safely
            }
            return NowPlayingTrack(isPlaying = false)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Reserved for notification badges
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Reserved for notification badges
    }
}
