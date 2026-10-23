package com.strathmore.CareConnect.data.repository

import com.strathmore.CareConnect.data.local.dao.NotificationDao
import com.strathmore.CareConnect.data.local.dao.PatientDao
import com.strathmore.CareConnect.data.local.dao.RuleThresholdDao
import com.strathmore.CareConnect.data.local.dao.SymptomDao
import com.strathmore.CareConnect.data.local.entities.Notification
import com.strathmore.CareConnect.data.local.entities.Patient
import com.strathmore.CareConnect.data.local.entities.Symptom
import kotlinx.coroutines.flow.Flow

class SymptomRepository(
    private val symptomDao: SymptomDao,
    private val patientDao: PatientDao,
    ruleThresholdDao: RuleThresholdDao,
    notificationDao: NotificationDao
) {
    private val ruleEngine = RuleEngine(symptomDao, ruleThresholdDao, notificationDao)

    suspend fun logSymptom(
        patientId: Long,
        type: String,
        rating: Int,
        notes: String,
        weightKg: Double? = null,
        systolicBp: Int? = null,
        diastolicBp: Int? = null,
        medicationTaken: Boolean? = null,
        surveyNote: String? = null
    ): List<Notification> {
        val symptom = Symptom(
            patientId = patientId,
            type = type,
            rating = rating,
            date = System.currentTimeMillis(),
            notes = notes,
            weightKg = weightKg,
            systolicBp = systolicBp,
            diastolicBp = diastolicBp,
            medicationTaken = medicationTaken,
            surveyNote = surveyNote
        )
        val symptomId = symptomDao.insert(symptom)
        return ruleEngine.evaluateAfterLogging(patientId, symptomId, type)
    }

    fun getHistory(patientId: Long): Flow<List<Symptom>> =
        symptomDao.getHistoryForPatient(patientId)

    suspend fun getLatestEntry(patientId: Long): Symptom? =
        symptomDao.getMostRecent(patientId)

    suspend fun getSymptom(symptomId: Long): Symptom? =
        symptomDao.getById(symptomId)

    suspend fun updateSymptom(
        symptomId: Long,
        type: String,
        rating: Int,
        notes: String,
        weightKg: Double? = null,
        systolicBp: Int? = null,
        diastolicBp: Int? = null,
        medicationTaken: Boolean? = null,
        surveyNote: String? = null
    ) {
        val existing = symptomDao.getById(symptomId)
            ?: throw IllegalStateException("Symptom not found")
        symptomDao.update(
            existing.copy(
                type = type,
                rating = rating,
                notes = notes,
                weightKg = weightKg,
                systolicBp = systolicBp,
                diastolicBp = diastolicBp,
                medicationTaken = medicationTaken,
                surveyNote = surveyNote
            )
        )
        // Re-evaluate rules after an edit too — a severity change could newly trigger
        // (or should probably still check) the same rules a fresh log would.
        ruleEngine.evaluateAfterLogging(existing.patientId, symptomId, type)
    }

    suspend fun deleteSymptom(symptomId: Long) {
        symptomDao.softDelete(symptomId, System.currentTimeMillis())
    }

    fun getPatientsForCaregiver(caregiverId: Long): Flow<List<Patient>> =
        patientDao.getByCaregiver(caregiverId)
}