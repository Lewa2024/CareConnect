package com.strathmore.CareConnect.ui.nurse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.dao.BookingWithNames
import com.strathmore.CareConnect.data.local.dao.PostVisitNoteWithNurse
import com.strathmore.CareConnect.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookingRepository

    init {
        val db = (application as CareConnectApp).database
        repository = BookingRepository(db.bookingDao(), db.postVisitNoteDao(), db.paymentDao())
    }

    private val _bookings = MutableStateFlow<List<BookingWithNames>>(emptyList())
    val bookings: StateFlow<List<BookingWithNames>> = _bookings.asStateFlow()

    private val _requestState = MutableStateFlow<BookingRequestState>(BookingRequestState.Idle)
    val requestState: StateFlow<BookingRequestState> = _requestState.asStateFlow()

    private val _notes = MutableStateFlow<List<PostVisitNoteWithNurse>>(emptyList())
    val notes: StateFlow<List<PostVisitNoteWithNurse>> = _notes.asStateFlow()

    private val _noteSaveState = MutableStateFlow<BookingRequestState>(BookingRequestState.Idle)
    val noteSaveState: StateFlow<BookingRequestState> = _noteSaveState.asStateFlow()

    private val _hasAccess = MutableStateFlow(false)
    val hasAccess: StateFlow<Boolean> = _hasAccess.asStateFlow()

    private val _paymentState = MutableStateFlow<BookingRequestState>(BookingRequestState.Idle)
    val paymentState: StateFlow<BookingRequestState> = _paymentState.asStateFlow()

    fun requestBooking(patientId: Long, nurseId: Long, requestedVisitTime: Long, notes: String) {
        if (requestedVisitTime <= System.currentTimeMillis()) {
            _requestState.value = BookingRequestState.Error("Please choose a future date and time")
            return
        }
        viewModelScope.launch {
            _requestState.value = BookingRequestState.Saving
            try {
                repository.requestBooking(patientId, nurseId, requestedVisitTime, notes)
                _requestState.value = BookingRequestState.Saved
            } catch (e: Exception) {
                _requestState.value = BookingRequestState.Error(e.message ?: "Failed to send request")
            }
        }
    }

    fun resetRequestState() {
        _requestState.value = BookingRequestState.Idle
    }

    fun loadBookingsForPatient(patientId: Long) {
        viewModelScope.launch {
            repository.getBookingsForPatient(patientId).collect { _bookings.value = it }
        }
    }

    fun loadBookingsForNurse(nurseId: Long) {
        viewModelScope.launch {
            repository.getBookingsForNurse(nurseId).collect { _bookings.value = it }
        }
    }

    fun respondToBooking(bookingId: Long, accept: Boolean) {
        viewModelScope.launch {
            repository.respondToBooking(bookingId, accept)
        }
    }

    fun markCompleted(bookingId: Long) {
        viewModelScope.launch {
            repository.markCompleted(bookingId)
        }
    }

    fun checkAccess(nurseId: Long, patientId: Long) {
        viewModelScope.launch {
            _hasAccess.value = repository.hasAcceptedBooking(nurseId, patientId)
        }
    }

    fun addPostVisitNote(bookingId: Long, patientId: Long, nurseId: Long, note: String) {
        if (note.isBlank()) {
            _noteSaveState.value = BookingRequestState.Error("Please write something before saving")
            return
        }
        viewModelScope.launch {
            _noteSaveState.value = BookingRequestState.Saving
            try {
                repository.addPostVisitNote(bookingId, patientId, nurseId, note)
                _noteSaveState.value = BookingRequestState.Saved
            } catch (e: Exception) {
                _noteSaveState.value = BookingRequestState.Error(e.message ?: "Failed to save note")
            }
        }
    }

    fun resetNoteSaveState() {
        _noteSaveState.value = BookingRequestState.Idle
    }

    fun loadNotesForPatient(patientId: Long) {
        viewModelScope.launch {
            repository.getNotesForPatient(patientId).collect { _notes.value = it }
        }
    }

    fun recordPayment(bookingId: Long, amountText: String, mpesaCode: String) {
        val amount = amountText.trim().toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _paymentState.value = BookingRequestState.Error("Enter a valid amount")
            return
        }
        if (mpesaCode.isBlank()) {
            _paymentState.value = BookingRequestState.Error("Enter the M-Pesa confirmation code")
            return
        }
        viewModelScope.launch {
            _paymentState.value = BookingRequestState.Saving
            try {
                repository.recordPayment(bookingId, amount, mpesaCode)
                _paymentState.value = BookingRequestState.Saved
            } catch (e: Exception) {
                _paymentState.value = BookingRequestState.Error(e.message ?: "Failed to save payment")
            }
        }
    }

    fun resetPaymentState() {
        _paymentState.value = BookingRequestState.Idle
    }
}

sealed class BookingRequestState {
    data object Idle : BookingRequestState()
    data object Saving : BookingRequestState()
    data object Saved : BookingRequestState()
    data class Error(val message: String) : BookingRequestState()
}