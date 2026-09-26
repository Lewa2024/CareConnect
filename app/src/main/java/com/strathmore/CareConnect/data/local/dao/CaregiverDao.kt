package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.Caregiver
import kotlinx.coroutines.flow.Flow

@Dao
interface CaregiverDao {
    @Insert
    suspend fun insert(caregiver: Caregiver): Long

    @Update
    suspend fun update(caregiver: Caregiver)

    @Delete
    suspend fun delete(caregiver: Caregiver)

    @Query("SELECT * FROM caregivers WHERE caregiverId = :caregiverId")
    suspend fun getById(caregiverId: Long): Caregiver?

    @Query("SELECT * FROM caregivers WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: Long): Caregiver?

    @Query("SELECT * FROM caregivers")
    fun getAll(): Flow<List<Caregiver>>
}