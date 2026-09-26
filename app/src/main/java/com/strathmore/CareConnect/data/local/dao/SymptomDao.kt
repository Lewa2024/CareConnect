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

    @Delete
    suspend fun delete(symptom: Symptom)

    @Query("SELECT * FROM symptoms WHERE patientId = :patientId ORDER BY date DESC")
    fun getHistoryForPatient(patientId: Long): Flow<List<Symptom>>

    // Backing query for the rule engine later: symptoms at/above a given severity since a given time
    @Query("SELECT * FROM symptoms WHERE patientId = :patientId AND rating >= :minRating AND date >= :sinceDate")
    suspend fun getSevereSymptoms(patientId: Long, minRating: Int, sinceDate: Long): List<Symptom>
}