package com.strathmore.CareConnect

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.strathmore.CareConnect.data.local.AppDatabase
import java.io.File

class CareConnectApp : Application() {
    // Lazily created, single shared Room instance for the whole app (offline-first).
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    companion object {
        const val MEDICATION_REMINDER_CHANNEL_ID = "medication_reminders"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // osmdroid requires a "user agent" string identifying the app (per OpenStreetMap's
        // tile usage policy) and a writable folder to cache downloaded map tiles.
        org.osmdroid.config.Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MEDICATION_REMINDER_CHANNEL_ID,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to give a patient their scheduled medication"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}