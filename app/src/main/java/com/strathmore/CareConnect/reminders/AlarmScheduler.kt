package com.strathmore.CareConnect.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * Schedules and cancels medication reminder alarms. Uses AlarmManager's approximate
 * (inexact) scheduling, which needs no special permission and is accurate enough for
 * a daily medication reminder (fires within a few minutes of the set time).
 */
object AlarmScheduler {

    fun schedule(
        context: Context,
        medicationTimeId: Long,
        medicationName: String,
        dosage: String,
        patientName: String,
        hour: Int,
        minute: Int
    ) {
        val triggerTime = nextOccurrence(hour, minute)
        scheduleAt(context, medicationTimeId, medicationName, dosage, patientName, hour, minute, triggerTime)
    }

    /** Called by the receiver itself after firing, to queue the same reminder for tomorrow. */
    fun scheduleNext(
        context: Context,
        medicationTimeId: Long,
        medicationName: String,
        dosage: String,
        patientName: String,
        hour: Int,
        minute: Int
    ) {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        scheduleAt(context, medicationTimeId, medicationName, dosage, patientName, hour, minute, tomorrow.timeInMillis)
    }

    fun cancel(context: Context, medicationTimeId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, medicationTimeId, "", "", "", 0, 0)
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleAt(
        context: Context,
        medicationTimeId: Long,
        medicationName: String,
        dosage: String,
        patientName: String,
        hour: Int,
        minute: Int,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, medicationTimeId, medicationName, dosage, patientName, hour, minute)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    private fun buildPendingIntent(
        context: Context,
        medicationTimeId: Long,
        medicationName: String,
        dosage: String,
        patientName: String,
        hour: Int,
        minute: Int
    ): PendingIntent {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra(MedicationReminderReceiver.EXTRA_MEDICATION_TIME_ID, medicationTimeId)
            putExtra(MedicationReminderReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(MedicationReminderReceiver.EXTRA_DOSAGE, dosage)
            putExtra(MedicationReminderReceiver.EXTRA_PATIENT_NAME, patientName)
            putExtra(MedicationReminderReceiver.EXTRA_HOUR, hour)
            putExtra(MedicationReminderReceiver.EXTRA_MINUTE, minute)
        }
        return PendingIntent.getBroadcast(
            context,
            medicationTimeId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextOccurrence(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}