package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getById(userId: Long): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("SELECT COUNT(*) FROM users WHERE roleId = :roleId")
    suspend fun countByRole(roleId: Long): Int

    @Query("UPDATE users SET password = :newPasswordHash, salt = :newSalt WHERE email = :email")
    suspend fun updatePassword(email: String, newPasswordHash: String, newSalt: String): Int
}