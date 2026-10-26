package com.strathmore.CareConnect.data.repository

import android.content.Context
import com.strathmore.CareConnect.data.local.dao.MedicationDao
import com.strathmore.CareConnect.data.local.dao.MedicationTimeDao
import com.strathmore.CareConnect.data.local.dao.PatientDao
import com.strathmore.CareConnect.data.local.entities.Medication
import com.strathmore.CareConnect.data.local.entities.MedicationTime
import com.strathmore.CareConnect.reminders.AlarmScheduler
import kotlinx.coroutines.flow.Flow

class MedicationRepository(
    private val context: Context,
    private val medicationDao: MedicationDao,
    private val medicationTimeDao: MedicationTimeDao,
    private val patientDao: PatientDao
) {
    /** Creates a medication with the given daily times, and schedules a reminder for each. */
    suspend fun addMedication(
        patientId: Long,
        name: String,
        dosage: String,
        notes: String,
        times: List<Pair<Int, Int>> // (hour, minute) pairs
    ): Long {
        val medication = Medication(
            patientId = patientId,
            name = name.trim(),
            dosage = dosage.trim(),
            timesPerDay = times.size,
            notes = notes.trim()
        )
        val medicationId = medicationDao.insert(medication)
        val patientName = patientDao.getById(patientId)?.name ?: "your patient"

        times.forEach { (hour, minute) ->
            val medicationTimeId = medicationTimeDao.insert(
                MedicationTime(medicationId = medicationId, hour = hour, minute = minute)
            )
            AlarmScheduler.schedule(
                context = context,
                medicationTimeId = medicationTimeId,
                medicationName = medication.name,
                dosage = medication.dosage,
                patientName = patientName,
                hour = hour,
                minute = minute
            )
        }

        return medicationId
    }

    fun getActiveMedications(patientId: Long): Flow<List<Medication>> =
        medicationDao.getActiveForPatient(patientId)

    fun getTimesForMedication(medicationId: Long): Flow<List<MedicationTime>> =
        medicationTimeDao.getForMedication(medicationId)

    /** Stops a medication: cancels its reminders and marks it inactive (kept for history). */
    suspend fun stopMedication(medicationId: Long) {
        val times = medicationTimeDao.getForMedicationOnce(medicationId)
        times.forEach { AlarmScheduler.cancel(context, it.medicationTimeId) }
        medicationDao.deactivate(medicationId)
    }
}