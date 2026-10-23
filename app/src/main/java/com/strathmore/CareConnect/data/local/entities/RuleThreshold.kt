package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rule_thresholds")
data class RuleThreshold(
    @PrimaryKey(autoGenerate = true) val ruleThresholdId: Long = 0,
    val metric: String,
    val thresholdValue: Int,
    val consecutiveEntriesRequired: Int = 1
)