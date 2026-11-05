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
    val dateOfBirth: Long,       // stored as epoch millis; age is derived from this, not stored separately
    val sex: String = "",        // "Male", "Female", or "Other"
    val chiefComplaint: String = "",
    val historyOfIllness: String = "",       // History of Present Illness
    val pastMedicalHistory: String = "",
    val surgicalHistory: String = "",
    val familyHistory: String = "",
    val currentPlan: String = "",
    val heightCm: Double? = null,
    val baselineWeightKg: Double? = null,
    val baselineSystolicBp: Int? = null,
    val baselineDiastolicBp: Int? = null
)