package com.strathmore.CareConnect.data.repository

import com.strathmore.CareConnect.data.local.dao.PatientDao
import com.strathmore.CareConnect.data.local.dao.SymptomDao
import com.strathmore.CareConnect.data.local.entities.Patient
import com.strathmore.CareConnect.data.local.entities.Symptom
import kotlinx.coroutines.flow.Flow

class SymptomRepository(
    private val symptomDao: SymptomDao,
    private val patientDao: PatientDao
) {
    suspend fun logSymptom(
        patientId: Long,
        type: String,
        rating: Int,
        notes: String
    ): Long {
        val symptom = Symptom(
            patientId = patientId,
            type = type,
            rating = rating,
            date = System.currentTimeMillis(),
            notes = notes
        )
        return symptomDao.insert(symptom)
    }

    fun getHistory(patientId: Long): Flow<List<Symptom>> =
        symptomDao.getHistoryForPatient(patientId)

    suspend fun getSymptom(symptomId: Long): Symptom? =
        symptomDao.getById(symptomId)

    suspend fun updateSymptom(symptomId: Long, type: String, rating: Int, notes: String) {
        val existing = symptomDao.getById(symptomId)
            ?: throw IllegalStateException("Symptom not found")
        symptomDao.update(existing.copy(type = type, rating = rating, notes = notes))
    }

    suspend fun deleteSymptom(symptomId: Long) {
        symptomDao.softDelete(symptomId, System.currentTimeMillis())
    }

    fun getPatientsForCaregiver(caregiverId: Long): Flow<List<Patient>> =
        patientDao.getByCaregiver(caregiverId)
}