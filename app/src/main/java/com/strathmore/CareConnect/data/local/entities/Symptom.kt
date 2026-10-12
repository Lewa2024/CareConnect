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
    val type: String,     // e.g. "Pain", "Nausea", "Fatigue" — from SYMPTOM_TYPES
    val rating: Int,      // 1-10 severity scale, drives the rule engine
    val date: Long,       // epoch millis
    val notes: String,
    val status: String = "ACTIVE",   // ACTIVE or DELETED — soft-delete, row is never actually removed
    val deletedAt: Long? = null,     // epoch millis when marked DELETED, null while ACTIVE

    // Vitals — all optional, since not every check-in will include every measurement
    val weightKg: Double? = null,
    val systolicBp: Int? = null,     // e.g. 120 (the "120" in "120/80")
    val diastolicBp: Int? = null,    // e.g. 80 (the "80" in "120/80")
    val medicationTaken: Boolean? = null,  // null = not answered, true/false = answered
    val surveyNote: String? = null   // short free-text wellbeing/quality-of-life note
)