package com.strathmore.CareConnect.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.Notification
import com.strathmore.CareConnect.data.local.entities.Patient
import com.strathmore.CareConnect.data.local.entities.Symptom
import com.strathmore.CareConnect.data.repository.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SymptomRepository
    private val notificationDao = (application as CareConnectApp).database.notificationDao()
    private val caregiverDao = (application as CareConnectApp).database.caregiverDao()
        private val userDao = (application as CareConnectApp).database.userDao()

    init {
        val db = (application as CareConnectApp).database
        repository = SymptomRepository(db.symptomDao(), db.patientDao(), db.ruleThresholdDao(), db.notificationDao())
    }

    companion object {
        // Clinical priority order for which single alert to surface on the Home screen
        // when more than one rule fires from the same log event (lower number = shown
        // first). Rule 1 (URGENT, absolute threshold) always wins. Rule 2 and Rule 5 are
        // specific single-event signals, shown before the trend-based Rule 4 and Rule 7.
        // Rule 6 (medication) comes after those. Rule 3 (multi-symptom) is deliberately
        // last: it scans every symptom type the patient has ever logged and fires easily
        // once several types have history, so it's the most likely to be "noise" sitting
        // alongside a more specific rule — e.g. Rule 2 firing alongside Rule 3 should show
        // Rule 2.
        private val RULE_PRIORITY = mapOf(
            1 to 1,
            2 to 2,
            5 to 3,
            4 to 4,
            7 to 5,
            6 to 6,
            3 to 7
        )
    }

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    private val _caregiverFirstName = MutableStateFlow<String?>(null)
    val caregiverFirstName: StateFlow<String?> = _caregiverFirstName.asStateFlow()

    private val _latestEntry = MutableStateFlow<Symptom?>(null)
    val latestEntry: StateFlow<Symptom?> = _latestEntry.asStateFlow()

    // Kept as a List for minimal disruption to HomeScreen.kt, but now holds at most one
    // entry: the single highest-priority rule that fired from the most recent log event,
    // per RULE_PRIORITY above, rather than every rule that fired.
    private val _latestAlerts = MutableStateFlow<List<Notification>>(emptyList())
    val latestAlerts: StateFlow<List<Notification>> = _latestAlerts.asStateFlow()

    private val _trend = MutableStateFlow<List<Symptom>>(emptyList())
    val trend: StateFlow<List<Symptom>> = _trend.asStateFlow()

    fun loadPatient(caregiverId: Long) {
        viewModelScope.launch {
            val caregiver = caregiverDao.getById(caregiverId)
            _caregiverFirstName.value = caregiver?.let { userDao.getById(it.userId)?.firstName }
        }
        viewModelScope.launch {
            repository.getPatientsForCaregiver(caregiverId).collect { patients ->
                _patient.value = patients.firstOrNull()
                _patient.value?.let {
                    loadLatestEntry(it.patientId)
                    loadLatestAlerts(it.patientId)
                    loadTrend(it.patientId)
                }
            }
        }
    }

    private fun loadLatestEntry(patientId: Long) {
        viewModelScope.launch {
            _latestEntry.value = repository.getLatestEntry(patientId)
        }
    }

    private fun loadLatestAlerts(patientId: Long) {
        viewModelScope.launch {
            val batch = notificationDao.getMostRecentBatch(patientId)
            val topPriority = batch.minByOrNull { RULE_PRIORITY[it.ruleNumber] ?: Int.MAX_VALUE }
            _latestAlerts.value = listOfNotNull(topPriority)
        }
    }

    private fun loadTrend(patientId: Long) {
        viewModelScope.launch {
            _trend.value = repository.getRecentTrend(patientId)
        }
    }
}