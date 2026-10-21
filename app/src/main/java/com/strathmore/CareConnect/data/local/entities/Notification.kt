package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientId"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId")]
)
data class Notification(
    @PrimaryKey(autoGenerate = true) val notificationId: Long = 0,
    val patientId: Long,
    val message: String,
    val urgency: String,       // "ROUTINE" or "URGENT"
    val timestamp: Long,
    val isRead: Boolean = false,
    val triggeringSymptomId: Long? = null  // which Symptom entry caused this, if applicable
)