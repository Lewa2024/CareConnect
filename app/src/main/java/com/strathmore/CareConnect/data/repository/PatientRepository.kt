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
        sex: String,
        chiefComplaint: String,
        historyOfIllness: String,
        pastMedicalHistory: String,
        surgicalHistory: String,
        familyHistory: String,
        currentPlan: String,
        heightCm: Double? = null,
        baselineWeightKg: Double? = null,
        baselineSystolicBp: Int? = null,
        baselineDiastolicBp: Int? = null
    ): Long {
        val patient = Patient(
            caregiverId = caregiverId,
            name = name.trim(),
            dateOfBirth = dateOfBirthMillis,
            sex = sex,
            chiefComplaint = chiefComplaint.trim(),
            historyOfIllness = historyOfIllness.trim(),
            pastMedicalHistory = pastMedicalHistory.trim(),
            surgicalHistory = surgicalHistory.trim(),
            familyHistory = familyHistory.trim(),
            currentPlan = currentPlan.trim(),
            heightCm = heightCm,
            baselineWeightKg = baselineWeightKg,
            baselineSystolicBp = baselineSystolicBp,
            baselineDiastolicBp = baselineDiastolicBp
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

    suspend fun updatePatient(
        patientId: Long,
        name: String,
        dateOfBirthMillis: Long,
        sex: String,
        chiefComplaint: String,
        historyOfIllness: String,
        pastMedicalHistory: String,
        surgicalHistory: String,
        familyHistory: String,
        currentPlan: String,
        heightCm: Double? = null,
        baselineWeightKg: Double? = null,
        baselineSystolicBp: Int? = null,
        baselineDiastolicBp: Int? = null
    ) {
        val existing = patientDao.getById(patientId)
            ?: throw IllegalStateException("Patient not found")
        patientDao.update(
            existing.copy(
                name = name.trim(),
                dateOfBirth = dateOfBirthMillis,
                sex = sex,
                chiefComplaint = chiefComplaint.trim(),
                historyOfIllness = historyOfIllness.trim(),
                pastMedicalHistory = pastMedicalHistory.trim(),
                surgicalHistory = surgicalHistory.trim(),
                familyHistory = familyHistory.trim(),
                currentPlan = currentPlan.trim(),
                heightCm = heightCm,
                baselineWeightKg = baselineWeightKg,
                baselineSystolicBp = baselineSystolicBp,
                baselineDiastolicBp = baselineDiastolicBp
            )
        )
    }

    fun getPatientsForCaregiver(caregiverId: Long): Flow<List<Patient>> =
        patientDao.getByCaregiver(caregiverId)
}