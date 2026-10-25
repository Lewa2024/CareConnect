package com.strathmore.CareConnect.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.dao.*
import com.strathmore.CareConnect.data.local.entities.RuleThreshold
import com.strathmore.CareConnect.ui.symptom.SYMPTOM_TYPES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CareConnectApp).database
    private val ruleThresholdDao: RuleThresholdDao = db.ruleThresholdDao()
    private val symptomDao: SymptomDao = db.symptomDao()
    private val userDao: UserDao = db.userDao()
    private val patientDao: PatientDao = db.patientDao()
    private val roleDao: RoleDao = db.roleDao()
    private val notificationDao: NotificationDao = db.notificationDao()

    private val _thresholds = MutableStateFlow<List<RuleThreshold>>(emptyList())
    val thresholds: StateFlow<List<RuleThreshold>> = _thresholds.asStateFlow()

    private val _saveState = MutableStateFlow<AdminSaveState>(AdminSaveState.Idle)
    val saveState: StateFlow<AdminSaveState> = _saveState.asStateFlow()

    data class SystemStats(
        val totalCaregivers: Int,
        val totalPatients: Int,
        val activeSymptomEntries: Int,
        val deletedSymptomEntries: Int,
        val urgentAlerts: Int,
        val routineAlerts: Int
    )

    private val _stats = MutableStateFlow<SystemStats?>(null)
    val stats: StateFlow<SystemStats?> = _stats.asStateFlow()

    // Privacy-safe: only the deletion timestamps, never the patient or symptom content.
    data class DeletionLogEntry(val deletedAt: Long)

    private val _deletionLog = MutableStateFlow<List<DeletionLogEntry>>(emptyList())
    val deletionLog: StateFlow<List<DeletionLogEntry>> = _deletionLog.asStateFlow()

    fun loadThresholds() {
        viewModelScope.launch {
            ruleThresholdDao.getAllFlow().collect { list ->
                _thresholds.value = list
            }
        }
    }

    fun saveThreshold(metric: String, thresholdValue: Int, consecutiveEntriesRequired: Int) {
        if (metric.isBlank()) {
            _saveState.value = AdminSaveState.Error("Please choose a symptom type")
            return
        }
        if (thresholdValue !in 1..10) {
            _saveState.value = AdminSaveState.Error("Severity must be between 1 and 10")
            return
        }
        if (consecutiveEntriesRequired < 1) {
            _saveState.value = AdminSaveState.Error("Consecutive entries must be at least 1")
            return
        }
        viewModelScope.launch {
            _saveState.value = AdminSaveState.Saving
            try {
                val existing = ruleThresholdDao.getByMetric(metric)
                val threshold = RuleThreshold(
                    ruleThresholdId = existing?.ruleThresholdId ?: 0,
                    metric = metric,
                    thresholdValue = thresholdValue,
                    consecutiveEntriesRequired = consecutiveEntriesRequired
                )
                if (existing != null) ruleThresholdDao.update(threshold) else ruleThresholdDao.insert(threshold)
                _saveState.value = AdminSaveState.Saved
            } catch (e: Exception) {
                _saveState.value = AdminSaveState.Error(e.message ?: "Failed to save threshold")
            }
        }
    }

    fun deleteThreshold(threshold: RuleThreshold) {
        viewModelScope.launch { ruleThresholdDao.delete(threshold) }
    }

    fun resetSaveState() {
        _saveState.value = AdminSaveState.Idle
    }

    fun unconfiguredMetrics(): List<String> {
        val configured = _thresholds.value.map { it.metric }.toSet()
        return SYMPTOM_TYPES.filter { it != "Other" && it !in configured }
    }

    fun loadSystemStats() {
        viewModelScope.launch {
            val caregiverRoleId = roleDao.getByName("Caregiver")?.roleId
            val totalCaregivers = caregiverRoleId?.let { userDao.countByRole(it) } ?: 0
            _stats.value = SystemStats(
                totalCaregivers = totalCaregivers,
                totalPatients = patientDao.countAll(),
                activeSymptomEntries = symptomDao.countActive(),
                deletedSymptomEntries = symptomDao.countDeleted(),
                urgentAlerts = notificationDao.countByUrgency("URGENT"),
                routineAlerts = notificationDao.countByUrgency("ROUTINE")
            )
        }
    }

    fun loadDeletionLog() {
        viewModelScope.launch {
            symptomDao.getAllDeletedSymptoms().collect { deleted ->
                _deletionLog.value = deleted
                    .mapNotNull { it.deletedAt }
                    .sortedDescending()
                    .map { DeletionLogEntry(it) }
            }
        }
    }
}

sealed class AdminSaveState {
    data object Idle : AdminSaveState()
    data object Saving : AdminSaveState()
    data object Saved : AdminSaveState()
    data class Error(val message: String) : AdminSaveState()
}