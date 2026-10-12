package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.Symptom
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Insert
    suspend fun insert(symptom: Symptom): Long

    @Update
    suspend fun update(symptom: Symptom)

    @Query("SELECT * FROM symptoms WHERE patientId = :patientId AND status = 'ACTIVE' ORDER BY date DESC")
    fun getHistoryForPatient(patientId: Long): Flow<List<Symptom>>

    @Query("SELECT * FROM symptoms WHERE symptomId = :symptomId")
    suspend fun getById(symptomId: Long): Symptom?

    @Query("UPDATE symptoms SET status = 'DELETED', deletedAt = :deletedAt WHERE symptomId = :symptomId")
    suspend fun softDelete(symptomId: Long, deletedAt: Long)

    // Admin-facing / audit query: every entry including soft-deleted ones, for traceability.
    @Query("SELECT * FROM symptoms WHERE patientId = :patientId ORDER BY date DESC")
    fun getFullHistoryIncludingDeleted(patientId: Long): Flow<List<Symptom>>

    // Backing query for the rule engine later: symptoms at/above a given severity since a given time
    @Query("SELECT * FROM symptoms WHERE patientId = :patientId AND status = 'ACTIVE' AND rating >= :minRating AND date >= :sinceDate")
    suspend fun getSevereSymptoms(patientId: Long, minRating: Int, sinceDate: Long): List<Symptom>

    
        @Query("SELECT * FROM symptoms WHERE patientId = :patientId AND status = 'ACTIVE' ORDER BY date DESC LIMIT 1")
    suspend fun getMostRecent(patientId: Long): Symptom?
}