package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.MedicationTime
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationTimeDao {
    @Insert
    suspend fun insert(medicationTime: MedicationTime): Long

    @Delete
    suspend fun delete(medicationTime: MedicationTime)

    @Query("SELECT * FROM medication_times WHERE medicationId = :medicationId ORDER BY hour ASC, minute ASC")
    fun getForMedication(medicationId: Long): Flow<List<MedicationTime>>

    @Query("SELECT * FROM medication_times WHERE medicationId = :medicationId ORDER BY hour ASC, minute ASC")
    suspend fun getForMedicationOnce(medicationId: Long): List<MedicationTime>

    @Query("DELETE FROM medication_times WHERE medicationId = :medicationId")
    suspend fun deleteAllForMedication(medicationId: Long)

    // Used by the boot receiver to re-schedule every active medication's alarms after a restart.
    @Query("""
        SELECT medication_times.* FROM medication_times
        INNER JOIN medications ON medication_times.medicationId = medications.medicationId
        WHERE medications.active = 1
    """)
    suspend fun getAllForActiveMedications(): List<MedicationTime>
}