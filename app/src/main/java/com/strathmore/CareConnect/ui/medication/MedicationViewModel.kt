package com.strathmore.CareConnect.ui.medication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.Medication
import com.strathmore.CareConnect.data.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MedicationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MedicationRepository

    init {
        val db = (application as CareConnectApp).database
        repository = MedicationRepository(application, db.medicationDao(), db.medicationTimeDao(), db.patientDao())
    }

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val _saveState = MutableStateFlow<MedicationSaveState>(MedicationSaveState.Idle)
    val saveState: StateFlow<MedicationSaveState> = _saveState.asStateFlow()

    fun loadMedications(patientId: Long) {
        viewModelScope.launch {
            repository.getActiveMedications(patientId).collect { list ->
                _medications.value = list
            }
        }
    }

    fun getTimesFlowFor(medicationId: Long) = repository.getTimesForMedication(medicationId)

    fun addMedication(
        patientId: Long,
        name: String,
        dosage: String,
        notes: String,
        times: List<Pair<Int, Int>>
    ) {
        if (name.isBlank()) {
            _saveState.value = MedicationSaveState.Error("Please enter the medication name")
            return
        }
        if (dosage.isBlank()) {
            _saveState.value = MedicationSaveState.Error("Please enter the dosage")
            return
        }
        if (times.isEmpty()) {
            _saveState.value = MedicationSaveState.Error("Add at least one time of day")
            return
        }
        viewModelScope.launch {
            _saveState.value = MedicationSaveState.Saving
            try {
                repository.addMedication(patientId, name, dosage, notes, times)
                _saveState.value = MedicationSaveState.Saved
            } catch (e: Exception) {
                _saveState.value = MedicationSaveState.Error(e.message ?: "Failed to save medication")
            }
        }
    }

    fun stopMedication(medicationId: Long) {
        viewModelScope.launch {
            repository.stopMedication(medicationId)
        }
    }

    fun resetSaveState() {
        _saveState.value = MedicationSaveState.Idle
    }
}

sealed class MedicationSaveState {
    data object Idle : MedicationSaveState()
    data object Saving : MedicationSaveState()
    data object Saved : MedicationSaveState()
    data class Error(val message: String) : MedicationSaveState()
}