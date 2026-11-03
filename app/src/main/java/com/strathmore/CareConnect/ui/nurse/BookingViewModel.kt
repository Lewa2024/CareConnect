package com.strathmore.CareConnect.ui.nurse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.dao.BookingWithNames
import com.strathmore.CareConnect.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookingRepository

    init {
        val db = (application as CareConnectApp).database
        repository = BookingRepository(db.bookingDao())
    }

    private val _bookings = MutableStateFlow<List<BookingWithNames>>(emptyList())
    val bookings: StateFlow<List<BookingWithNames>> = _bookings.asStateFlow()

    private val _requestState = MutableStateFlow<BookingRequestState>(BookingRequestState.Idle)
    val requestState: StateFlow<BookingRequestState> = _requestState.asStateFlow()

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
}

sealed class BookingRequestState {
    data object Idle : BookingRequestState()
    data object Saving : BookingRequestState()
    data object Saved : BookingRequestState()
    data class Error(val message: String) : BookingRequestState()
}