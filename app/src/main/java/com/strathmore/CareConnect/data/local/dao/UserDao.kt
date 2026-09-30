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

    @Query("SELECT * FROM users WHERE email = :email AND password = :passwordHash LIMIT 1")
    suspend fun login(email: String, passwordHash: String): User?

    @Query("UPDATE users SET password = :newPasswordHash WHERE email = :email")
    suspend fun updatePassword(email: String, newPasswordHash: String): Int
}