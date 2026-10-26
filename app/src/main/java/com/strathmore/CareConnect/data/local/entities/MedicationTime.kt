package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_times",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["medicationId"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId")]
)
data class MedicationTime(
    @PrimaryKey(autoGenerate = true) val medicationTimeId: Long = 0,
    val medicationId: Long,
    val hour: Int,    // 0-23
    val minute: Int   // 0-59
)