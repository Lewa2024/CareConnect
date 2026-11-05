package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "post_visit_notes",
    foreignKeys = [
        ForeignKey(
            entity = Booking::class,
            parentColumns = ["bookingId"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        ),
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
    indices = [Index("bookingId"), Index("patientId"), Index("nurseId")]
)
data class PostVisitNote(
    @PrimaryKey(autoGenerate = true) val noteId: Long = 0,
    val bookingId: Long,
    val patientId: Long,
    val nurseId: Long,
    val note: String,
    val createdAt: Long
)