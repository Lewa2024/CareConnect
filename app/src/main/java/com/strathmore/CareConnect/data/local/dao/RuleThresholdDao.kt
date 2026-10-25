package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.RuleThreshold
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleThresholdDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ruleThreshold: RuleThreshold): Long

    @Update
    suspend fun update(ruleThreshold: RuleThreshold)

    @Delete
    suspend fun delete(ruleThreshold: RuleThreshold)

    @Query("SELECT * FROM rule_thresholds WHERE metric = :metric LIMIT 1")
    suspend fun getByMetric(metric: String): RuleThreshold?

    @Query("SELECT * FROM rule_thresholds ORDER BY metric ASC")
    fun getAllFlow(): Flow<List<RuleThreshold>>

    @Query("SELECT * FROM rule_thresholds")
    suspend fun getAll(): List<RuleThreshold>
}