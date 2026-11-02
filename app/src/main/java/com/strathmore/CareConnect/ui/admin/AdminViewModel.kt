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

    private val _caregivers = MutableStateFlow<List<CaregiverDirectoryEntry>>(emptyList())
    val caregivers: StateFlow<List<CaregiverDirectoryEntry>> = _caregivers.asStateFlow()

    private val _ruleFiringCounts = MutableStateFlow<List<RuleFiringCount>>(emptyList())
    val ruleFiringCounts: StateFlow<List<RuleFiringCount>> = _ruleFiringCounts.asStateFlow()

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
        loadRuleFiringCounts()
    }

    private fun loadRuleFiringCounts() {
        viewModelScope.launch {
            _ruleFiringCounts.value = notificationDao.getRuleFiringCounts()
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

    fun loadCaregiverDirectory() {
        viewModelScope.launch {
            userDao.getCaregiverDirectory().collect { list ->
                _caregivers.value = list
            }
        }
    }

       fun setCaregiverActive(userId: Long, active: Boolean) {
        viewModelScope.launch {
            userDao.setActive(userId, active)
        }
    }

    private val _adminUser = MutableStateFlow<com.strathmore.CareConnect.data.local.entities.User?>(null)
    val adminUser: StateFlow<com.strathmore.CareConnect.data.local.entities.User?> = _adminUser.asStateFlow()

    private val _passwordChangeState = MutableStateFlow<AdminSaveState>(AdminSaveState.Idle)
    val passwordChangeState: StateFlow<AdminSaveState> = _passwordChangeState.asStateFlow()

    fun loadAdminProfile(userId: Long) {
        viewModelScope.launch {
            _adminUser.value = userDao.getById(userId)
        }
    }

    fun changeAdminPassword(currentPassword: String, newPassword: String) {
        val admin = _adminUser.value
        if (admin == null) {
            _passwordChangeState.value = AdminSaveState.Error("Account not loaded")
            return
        }
        if (newPassword.length < 6) {
            _passwordChangeState.value = AdminSaveState.Error("New password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _passwordChangeState.value = AdminSaveState.Saving
            try {
                val currentHash = hash(currentPassword, admin.salt)
                if (currentHash != admin.password) {
                    _passwordChangeState.value = AdminSaveState.Error("Current password is incorrect")
                    return@launch
                }
                val newSalt = generateSalt()
                userDao.updatePassword(admin.email, hash(newPassword, newSalt), newSalt)
                _passwordChangeState.value = AdminSaveState.Saved
            } catch (e: Exception) {
                _passwordChangeState.value = AdminSaveState.Error(e.message ?: "Failed to change password")
            }
        }
    }

    fun resetPasswordChangeState() {
        _passwordChangeState.value = AdminSaveState.Idle
    }

    private fun generateSalt(): String {
        val saltBytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hash(raw: String, salt: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest((salt + raw).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
sealed class AdminSaveState {
    data object Idle : AdminSaveState()
    data object Saving : AdminSaveState()
    data object Saved : AdminSaveState()
    data class Error(val message: String) : AdminSaveState()
}