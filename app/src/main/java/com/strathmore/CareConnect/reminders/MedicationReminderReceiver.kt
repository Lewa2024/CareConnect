package com.strathmore.CareConnect.reminders

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.MainActivity

/**
 * Fires when a medication reminder alarm goes off. Shows a notification naming the
 * patient, medicine, and dosage, then re-schedules itself for the same time tomorrow —
 * AlarmManager's approximate repeating alarms aren't reliable long-term on their own,
 * so each firing is responsible for queuing the next one.
 */
class MedicationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationTimeId = intent.getLongExtra(EXTRA_MEDICATION_TIME_ID, -1L)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "medication"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val patientName = intent.getStringExtra(EXTRA_PATIENT_NAME) ?: "your patient"
        val hour = intent.getIntExtra(EXTRA_HOUR, 0)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)

        showNotification(context, medicationTimeId, medicationName, dosage, patientName)

        // Re-schedule for the same time tomorrow, keeping the reminder recurring daily.
        if (medicationTimeId != -1L) {
            AlarmScheduler.scheduleNext(context, medicationTimeId, medicationName, dosage, patientName, hour, minute)
        }
    }

    private fun showNotification(
        context: Context,
        medicationTimeId: Long,
        medicationName: String,
        dosage: String,
        patientName: String
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            medicationTimeId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CareConnectApp.MEDICATION_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Time for $patientName's medication")
            .setContentText("$medicationName${if (dosage.isNotBlank()) " ($dosage)" else ""}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(medicationTimeId.toInt(), notification)
    }

    companion object {
        const val EXTRA_MEDICATION_TIME_ID = "medicationTimeId"
        const val EXTRA_MEDICATION_NAME = "medicationName"
        const val EXTRA_DOSAGE = "dosage"
        const val EXTRA_PATIENT_NAME = "patientName"
        const val EXTRA_HOUR = "hour"
        const val EXTRA_MINUTE = "minute"
    }
}