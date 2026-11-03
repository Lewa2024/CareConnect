package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "nurses",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"], unique = true)]
)
data class Nurse(
    @PrimaryKey(autoGenerate = true) val nurseId: Long = 0,
    val userId: Long,
    val qualification: String,
    val available: Boolean = true,
    val verified: Boolean = false // reserved for future Admin verification workflow
)