package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.RuleThreshold

@Dao
interface RuleThresholdDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ruleThreshold: RuleThreshold): Long

    @Query("SELECT * FROM rule_thresholds WHERE metric = :metric LIMIT 1")
    suspend fun getByMetric(metric: String): RuleThreshold?

    @Query("SELECT * FROM rule_thresholds")
    suspend fun getAll(): List<RuleThreshold>
}