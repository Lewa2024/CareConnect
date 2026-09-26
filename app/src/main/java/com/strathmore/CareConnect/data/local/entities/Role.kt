package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class Role(
    @PrimaryKey(autoGenerate = true) val roleId: Long = 0,
    val name: String,        // e.g. "Caregiver", "Nurse", "Administrator"
    val description: String
)