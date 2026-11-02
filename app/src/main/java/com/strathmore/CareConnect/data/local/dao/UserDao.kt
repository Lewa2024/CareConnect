package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.User
import kotlinx.coroutines.flow.Flow

data class CaregiverDirectoryEntry(
    val userId: Long,
    val caregiverId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val active: Boolean,
    val patientCount: Int
)

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getById(userId: Long): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("UPDATE users SET password = :newPasswordHash, salt = :newSalt WHERE email = :email")
    suspend fun updatePassword(email: String, newPasswordHash: String, newSalt: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE roleId = :roleId")
    suspend fun countByRole(roleId: Long): Int

    @Query("UPDATE users SET active = :active WHERE userId = :userId")
    suspend fun setActive(userId: Long, active: Boolean)

    @Query("""
        SELECT
            u.userId AS userId,
            c.caregiverId AS caregiverId,
            u.firstName AS firstName,
            u.lastName AS lastName,
            u.email AS email,
            u.phoneNumber AS phoneNumber,
            u.active AS active,
            (SELECT COUNT(*) FROM patients p WHERE p.caregiverId = c.caregiverId) AS patientCount
        FROM users u
        INNER JOIN caregivers c ON c.userId = u.userId
        INNER JOIN roles r ON r.roleId = u.roleId
        WHERE r.name = 'Caregiver'
        ORDER BY u.firstName ASC
    """)
    fun getCaregiverDirectory(): Flow<List<CaregiverDirectoryEntry>>
}