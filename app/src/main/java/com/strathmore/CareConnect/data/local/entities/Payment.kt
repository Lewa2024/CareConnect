package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Booking::class,
            parentColumns = ["bookingId"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookingId", unique = true)] // one payment record per booking
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val paymentId: Long = 0,
    val bookingId: Long,
    val amountKsh: Double,
    val mpesaCode: String,
    val paidAt: Long // when the caregiver recorded it
)