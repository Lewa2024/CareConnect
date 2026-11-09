package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientId"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Nurse::class,
            parentColumns = ["nurseId"],
            childColumns = ["nurseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId"), Index("nurseId")]
)
data class Booking(
    @PrimaryKey(autoGenerate = true) val bookingId: Long = 0,
    val patientId: Long,
    val nurseId: Long,
    val requestedAt: Long,       // when the caregiver made the request
    val requestedVisitTime: Long, // the date/time they're asking the nurse to visit
    val notes: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, DECLINED, COMPLETED
    val requestedAmountKsh: Double? = null // set by the nurse when requesting payment for a completed visit
)