package com.strathmore.CareConnect.ui.symptom

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.Symptom
import com.strathmore.CareConnect.data.repository.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SymptomHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SymptomRepository

    init {
        val db = (application as CareConnectApp).database
        repository = SymptomRepository(db.symptomDao(), db.patientDao())
    }

    private val _history = MutableStateFlow<List<Symptom>>(emptyList())
    val history: StateFlow<List<Symptom>> = _history.asStateFlow()

    private val _editingSymptom = MutableStateFlow<Symptom?>(null)
    val editingSymptom: StateFlow<Symptom?> = _editingSymptom.asStateFlow()

    private val _editState = MutableStateFlow<SymptomEditState>(SymptomEditState.Idle)
    val editState: StateFlow<SymptomEditState> = _editState.asStateFlow()

    fun loadHistory(patientId: Long) {
        viewModelScope.launch {
            repository.getHistory(patientId).collect { list ->
                _history.value = list
            }
        }
    }

    fun deleteSymptom(symptomId: Long) {
        viewModelScope.launch {
            repository.deleteSymptom(symptomId)
        }
    }

    fun loadSymptomForEdit(symptomId: Long) {
        viewModelScope.launch {
            _editingSymptom.value = repository.getSymptom(symptomId)
        }
    }

    fun updateSymptom(
        symptomId: Long,
        type: String,
        rating: Int,
        notes: String,
        weightKg: Double?,
        systolicBp: Int?,
        diastolicBp: Int?,
        medicationTaken: Boolean?,
        surveyNote: String?
    ) {
        if (type.isBlank()) {
            _editState.value = SymptomEditState.Error("Please select a symptom type")
            return
        }
        if (rating !in 1..10) {
            _editState.value = SymptomEditState.Error("Severity must be between 1 and 10")
            return
        }
        viewModelScope.launch {
            _editState.value = SymptomEditState.Saving
            try {
                repository.updateSymptom(
                    symptomId, type, rating, notes,
                    weightKg, systolicBp, diastolicBp, medicationTaken, surveyNote
                )
                _editState.value = SymptomEditState.Saved
            } catch (e: Exception) {
                _editState.value = SymptomEditState.Error(e.message ?: "Failed to update symptom")
            }
        }
    }

    fun resetEditState() {
        _editState.value = SymptomEditState.Idle
    }
}

sealed class SymptomEditState {
    data object Idle : SymptomEditState()
    data object Saving : SymptomEditState()
    data object Saved : SymptomEditState()
    data class Error(val message: String) : SymptomEditState()
}