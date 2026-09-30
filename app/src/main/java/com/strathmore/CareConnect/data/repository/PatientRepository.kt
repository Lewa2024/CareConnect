package com.strathmore.CareConnect.data.repository

import com.strathmore.CareConnect.data.local.dao.PatientDao
import com.strathmore.CareConnect.data.local.entities.Patient
import kotlinx.coroutines.flow.Flow

class PatientRepository(
    private val patientDao: PatientDao
) {
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

    suspend fun getPatient(patientId: Long): Patient? =
        patientDao.getById(patientId)

    suspend fun updatePatientName(patientId: Long, newName: String) {
        val existing = patientDao.getById(patientId)
            ?: throw IllegalStateException("Patient not found")
        patientDao.update(existing.copy(name = newName.trim()))
    }

    fun getPatientsForCaregiver(caregiverId: Long): Flow<List<Patient>> =
        patientDao.getByCaregiver(caregiverId)
}