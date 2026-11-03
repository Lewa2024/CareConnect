package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.Location

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: Location): Long

    @Update
    suspend fun update(location: Location)

    @Query("SELECT * FROM locations WHERE nurseId = :nurseId LIMIT 1")
    suspend fun getByNurseId(nurseId: Long): Location?
}