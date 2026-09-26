package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.Role

@Dao
interface RoleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(role: Role): Long

    @Query("SELECT * FROM roles WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Role?

    @Query("SELECT * FROM roles WHERE roleId = :roleId")
    suspend fun getById(roleId: Long): Role?
}