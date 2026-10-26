package com.strathmore.CareConnect.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.strathmore.CareConnect.CareConnectApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AlarmManager alarms are cleared when the device reboots, so every active medication's
 * reminders need to be re-scheduled from scratch once the phone turns back on.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val app = context.applicationContext as CareConnectApp
        CoroutineScope(Dispatchers.IO).launch {
            val medicationTimeDao = app.database.medicationTimeDao()
            val medicationDao = app.database.medicationDao()
            val patientDao = app.database.patientDao()

            val times = medicationTimeDao.getAllForActiveMedications()
            times.forEach { time ->
                val medication = medicationDao.getById(time.medicationId) ?: return@forEach
                val patient = patientDao.getById(medication.patientId)
                AlarmScheduler.schedule(
                    context = context,
                    medicationTimeId = time.medicationTimeId,
                    medicationName = medication.name,
                    dosage = medication.dosage,
                    patientName = patient?.name ?: "your patient",
                    hour = time.hour,
                    minute = time.minute
                )
            }
        }
    }
}