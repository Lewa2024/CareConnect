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
}