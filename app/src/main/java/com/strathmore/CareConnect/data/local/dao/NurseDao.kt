package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.Nurse
import kotlinx.coroutines.flow.Flow

data class NurseWithLocation(
    val nurseId: Long,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val qualification: String,
    val available: Boolean,
    val city: String,
    val latitude: Double,
    val longitude: Double
)

data class NurseDirectoryEntry(
    val nurseId: Long,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val qualification: String,
    val city: String?,
    val available: Boolean,
    val verified: Boolean,
    val active: Boolean,
    val bookingCount: Int
)

@Dao
interface NurseDao {
    @Insert
    suspend fun insert(nurse: Nurse): Long

    @Query("SELECT * FROM nurses WHERE nurseId = :nurseId")
    suspend fun getById(nurseId: Long): Nurse?

    @Query("SELECT * FROM nurses WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: Long): Nurse?

    @Query("UPDATE nurses SET available = :available WHERE nurseId = :nurseId")
    suspend fun setAvailable(nurseId: Long, available: Boolean)

    @Query("""
        SELECT n.nurseId AS nurseId, n.userId AS userId, u.firstName AS firstName, u.lastName AS lastName,
               n.qualification AS qualification, n.available AS available,
               l.city AS city, l.latitude AS latitude, l.longitude AS longitude
        FROM nurses n
        INNER JOIN users u ON u.userId = n.userId
        INNER JOIN locations l ON l.nurseId = n.nurseId
        WHERE n.available = 1
    """)
    fun getAvailableNursesWithLocation(): Flow<List<NurseWithLocation>>

    @Query("UPDATE nurses SET verified = :verified WHERE nurseId = :nurseId")
    suspend fun setVerified(nurseId: Long, verified: Boolean)

    @Query("""
        SELECT n.nurseId AS nurseId, n.userId AS userId, u.firstName AS firstName, u.lastName AS lastName,
               u.email AS email, u.phoneNumber AS phoneNumber, n.qualification AS qualification,
               l.city AS city, n.available AS available, n.verified AS verified, u.active AS active,
               (SELECT COUNT(*) FROM bookings b WHERE b.nurseId = n.nurseId) AS bookingCount
        FROM nurses n
        INNER JOIN users u ON u.userId = n.userId
        LEFT JOIN locations l ON l.nurseId = n.nurseId
        ORDER BY u.firstName ASC
    """)
    fun getNurseDirectory(): Flow<List<NurseDirectoryEntry>>
}