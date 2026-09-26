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

    fun getPatientsForCaregiver(caregiverId: Long): Flow<List<Patient>> =
        patientDao.getByCaregiver(caregiverId)
}