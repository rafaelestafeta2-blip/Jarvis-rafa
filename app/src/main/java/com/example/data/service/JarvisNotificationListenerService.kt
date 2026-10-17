package com.example.data.service

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.db.JarvisDatabase
import com.example.data.db.NotificationLogEntity
import com.example.data.voice.VoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JarvisNotificationListenerService : NotificationListenerService() {

    private var voiceManager: VoiceManager? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        voiceManager = VoiceManager(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val sbnObj = sbn ?: return

        // Ignore notifications from JARVIS itself or persistent system ongoing services
        if (sbnObj.packageName == packageName || sbnObj.isOngoing) return

        val extras = sbnObj.notification?.extras ?: return
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        // Resolve friendly application name
        val pm = applicationContext.packageManager
        val appLabel = try {
            val appInfo = pm.getApplicationInfo(sbnObj.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbnObj.packageName
        }

        // Persist notification log to Room
        scope.launch {
            try {
                val dao = JarvisDatabase.getDatabase(applicationContext).jarvisDao()
                dao.insertNotificationLog(
                    NotificationLogEntity(
                        packageName = sbnObj.packageName,
                        appLabel = appLabel,
                        title = title,
                        contentText = text
                    )
                )
            } catch (e: Exception) {
                // Ignore DB log insert errors
            }
        }

        // Real-time voice readout
        val announcement = if (text.isNotBlank()) {
            "Sir, nova mensagem em $appLabel de $title. $text"
        } else {
            "Sir, notificação recebida de $appLabel. $title"
        }

        voiceManager?.speak(announcement)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager?.destroy()
    }
}
