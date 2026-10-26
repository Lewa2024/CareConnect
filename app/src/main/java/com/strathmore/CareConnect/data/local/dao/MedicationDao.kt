package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.Medication
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Insert
    suspend fun insert(medication: Medication): Long

    @Update
    suspend fun update(medication: Medication)

    @Query("SELECT * FROM medications WHERE medicationId = :medicationId")
    suspend fun getById(medicationId: Long): Medication?

    @Query("SELECT * FROM medications WHERE patientId = :patientId AND active = 1 ORDER BY name ASC")
    fun getActiveForPatient(patientId: Long): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE active = 1")
    suspend fun getAllActive(): List<Medication>

    @Query("UPDATE medications SET active = 0 WHERE medicationId = :medicationId")
    suspend fun deactivate(medicationId: Long)
}