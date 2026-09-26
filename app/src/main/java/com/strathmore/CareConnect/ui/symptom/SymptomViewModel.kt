package com.strathmore.CareConnect.ui.symptom

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.Patient
import com.strathmore.CareConnect.data.repository.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SymptomViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SymptomRepository

    init {
        val db = (application as CareConnectApp).database
        repository = SymptomRepository(db.symptomDao(), db.patientDao())
    }

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun loadPatients(caregiverId: Long) {
        viewModelScope.launch {
            repository.getPatientsForCaregiver(caregiverId).collect { list ->
                _patients.value = list
            }
        }
    }

    fun logSymptom(patientId: Long, type: String, rating: Int, notes: String) {
        if (type.isBlank()) {
            _saveState.value = SaveState.Error("Please select a symptom type")
            return
        }
        if (rating !in 1..10) {
            _saveState.value = SaveState.Error("Severity must be between 1 and 10")
            return
        }
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                repository.logSymptom(patientId, type, rating, notes)
                _saveState.value = SaveState.Saved
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to save symptom")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}

sealed class SaveState {
    data object Idle : SaveState()
    data object Saving : SaveState()
    data object Saved : SaveState()
    data class Error(val message: String) : SaveState()
}