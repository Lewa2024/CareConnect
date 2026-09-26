package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "patients",
    foreignKeys = [
        ForeignKey(
            entity = Caregiver::class,
            parentColumns = ["caregiverId"],
            childColumns = ["caregiverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("caregiverId")]
)
data class Patient(
    @PrimaryKey(autoGenerate = true) val patientId: Long = 0,
    val caregiverId: Long,
    val name: String,
    val dateOfBirth: Long,       // stored as epoch millis, converted via Converters (next step)
    val medicalHistory: String,
    val medication: String
)