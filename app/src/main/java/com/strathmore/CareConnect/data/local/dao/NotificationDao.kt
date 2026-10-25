package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: Notification): Long

    @Query("SELECT * FROM notifications WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getForPatient(patientId: Long): Flow<List<Notification>>

    @Query("SELECT * FROM notifications WHERE patientId = :patientId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecent(patientId: Long): Notification?

    @Query("UPDATE notifications SET isRead = 1 WHERE notificationId = :notificationId")
    suspend fun markAsRead(notificationId: Long)

        @Query("SELECT COUNT(*) FROM notifications WHERE urgency = :urgency")
    suspend fun countByUrgency(urgency: String): Int
}