package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rule_thresholds")
data class RuleThreshold(
    @PrimaryKey(autoGenerate = true) val ruleThresholdId: Long = 0,
    val metric: String,                    // matches a value from SYMPTOM_TYPES, e.g. "Pain"
    val thresholdValue: Int,               // severity (1-10) at/above which this counts
    val consecutiveEntriesRequired: Int = 1 // how many recent logged entries of this type must hit the threshold
)