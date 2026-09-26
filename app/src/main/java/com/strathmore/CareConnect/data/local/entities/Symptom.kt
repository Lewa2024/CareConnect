package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "symptoms",
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
data class Symptom(
    @PrimaryKey(autoGenerate = true) val symptomId: Long = 0,
    val patientId: Long,
    val type: String,     // e.g. "Fatigue", "Nausea", "Pain"
    val rating: Int,      // 1-10 severity scale, drives the rule engine
    val date: Long,       // epoch millis
    val notes: String
)