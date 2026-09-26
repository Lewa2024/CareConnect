package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Insert
    suspend fun insert(patient: Patient): Long

    @Update
    suspend fun update(patient: Patient)

    @Delete
    suspend fun delete(patient: Patient)

    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    suspend fun getById(patientId: Long): Patient?

    @Query("SELECT * FROM patients WHERE caregiverId = :caregiverId")
    fun getByCaregiver(caregiverId: Long): Flow<List<Patient>>
}