package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.Symptom
import kotlinx.coroutines.flow.Flow

data class AdminDeletionLogEntry(
    val symptomId: Long,
    val patientName: String,
    val type: String,
    val loggedAt: Long,
    val deletedAt: Long?
)

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

    @Query("UPDATE symptoms SET status = 'ACTIVE', deletedAt = NULL WHERE symptomId = :symptomId")
    suspend fun restore(symptomId: Long)

    @Query("SELECT * FROM symptoms WHERE patientId = :patientId AND status = 'DELETED' ORDER BY deletedAt DESC")
    fun getDeletedForPatient(patientId: Long): Flow<List<Symptom>>

    // Admin-facing / audit query: every entry including soft-deleted ones, for traceability.
    @Query("SELECT * FROM symptoms WHERE patientId = :patientId ORDER BY date DESC")
    fun getFullHistoryIncludingDeleted(patientId: Long): Flow<List<Symptom>>

    @Query("SELECT * FROM symptoms WHERE status = 'DELETED' ORDER BY deletedAt DESC")
    fun getAllDeletedSymptoms(): Flow<List<Symptom>>

    @Query("""
        SELECT s.symptomId AS symptomId, p.name AS patientName, s.type AS type,
               s.date AS loggedAt, s.deletedAt AS deletedAt
        FROM symptoms s
        INNER JOIN patients p ON p.patientId = s.patientId
        WHERE s.status = 'DELETED'
        ORDER BY s.deletedAt DESC
    """)
    fun getDeletionLogWithDetails(): Flow<List<AdminDeletionLogEntry>>

    @Query("SELECT COUNT(*) FROM symptoms WHERE status = 'ACTIVE'")
    suspend fun countActive(): Int

    @Query("SELECT COUNT(*) FROM symptoms WHERE status = 'DELETED'")
    suspend fun countDeleted(): Int

    // Backing query for the rule engine later: symptoms at/above a given severity since a given time
    @Query("SELECT * FROM symptoms WHERE patientId = :patientId AND status = 'ACTIVE' AND rating >= :minRating AND date >= :sinceDate")
    suspend fun getSevereSymptoms(patientId: Long, minRating: Int, sinceDate: Long): List<Symptom>

    @Query("SELECT * FROM symptoms WHERE patientId = :patientId AND status = 'ACTIVE' ORDER BY date DESC LIMIT 1")
    suspend fun getMostRecent(patientId: Long): Symptom?

    @Query("SELECT * FROM symptoms WHERE patientId = :patientId AND status = 'ACTIVE' ORDER BY date DESC LIMIT 10")
    suspend fun getRecentForTrend(patientId: Long): List<Symptom>

    @Query("SELECT * FROM symptoms WHERE patientId = :patientId AND type = :type AND status = 'ACTIVE' ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentByType(patientId: Long, type: String, limit: Int): List<Symptom>

    @Query("SELECT * FROM symptoms WHERE patientId = :patientId AND type = :type AND status = 'ACTIVE' ORDER BY date DESC")
    suspend fun getAllByType(patientId: Long, type: String): List<Symptom>

    @Query("SELECT DISTINCT type FROM symptoms WHERE patientId = :patientId AND status = 'ACTIVE'")
    suspend fun getDistinctTypesForPatient(patientId: Long): List<String>
}