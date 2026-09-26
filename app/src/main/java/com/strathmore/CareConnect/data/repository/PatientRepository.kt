package com.strathmore.CareConnect.data.repository

import com.strathmore.CareConnect.data.local.dao.PatientDao
import com.strathmore.CareConnect.data.local.entities.Patient
import kotlinx.coroutines.flow.Flow

class PatientRepository(
    private val patientDao: PatientDao
) {
    /** Adds a new patient under this caregiver. Returns the new patientId. */
    suspend fun addPatient(
        caregiverId: Long,
        name: String,
        dateOfBirthMillis: Long,
        medicalHistory: String,
        medication: String
    ): Long {
        val patient = Patient(
            caregiverId = caregiverId,
            name = name.trim(),
            dateOfBirth = dateOfBirthMillis,
            medicalHistory = medicalHistory.trim(),
            medication = medication.trim()
        )
        return patientDao.insert(patient)
    }

    fun getPatientsForCaregiver(caregiverId: Long): Flow<List<Patient>> =
        patientDao.getByCaregiver(caregiverId)
}