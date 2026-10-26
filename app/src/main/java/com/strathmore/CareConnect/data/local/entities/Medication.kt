package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
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
data class Medication(
    @PrimaryKey(autoGenerate = true) val medicationId: Long = 0,
    val patientId: Long,
    val name: String,
    val dosage: String,
    val timesPerDay: Int,
    val notes: String = "",
    val active: Boolean = true
)