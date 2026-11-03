package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = Nurse::class,
            parentColumns = ["nurseId"],
            childColumns = ["nurseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["nurseId"], unique = true)]
)
data class Location(
    @PrimaryKey(autoGenerate = true) val locationId: Long = 0,
    val nurseId: Long,
    val city: String,
    val latitude: Double,
    val longitude: Double
)