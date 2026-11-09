package com.strathmore.CareConnect.ui.nurse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.dao.BookingWithNames
import com.strathmore.CareConnect.data.local.dao.PostVisitNoteWithNurse
import com.strathmore.CareConnect.data.repository.BookingRepository
import com.strathmore.CareConnect.data.repository.DarajaRepository
import com.strathmore.CareConnect.data.repository.StkPushOutcome
import com.strathmore.CareConnect.data.repository.StkPushStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookingRepository
    private val darajaRepository = DarajaRepository()

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

    private val _requestPaymentState = MutableStateFlow<BookingRequestState>(BookingRequestState.Idle)
    val requestPaymentState: StateFlow<BookingRequestState> = _requestPaymentState.asStateFlow()

    private val _mpesaState = MutableStateFlow<MpesaPaymentState>(MpesaPaymentState.Idle)
    val mpesaState: StateFlow<MpesaPaymentState> = _mpesaState.asStateFlow()

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

    fun requestPayment(bookingId: Long, amountText: String) {
        val amount = amountText.trim().toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _requestPaymentState.value = BookingRequestState.Error("Enter a valid amount")
            return
        }
        viewModelScope.launch {
            _requestPaymentState.value = BookingRequestState.Saving
            try {
                repository.requestPayment(bookingId, amount)
                _requestPaymentState.value = BookingRequestState.Saved
            } catch (e: Exception) {
                _requestPaymentState.value = BookingRequestState.Error(e.message ?: "Failed to request payment")
            }
        }
    }

    fun resetRequestPaymentState() {
        _requestPaymentState.value = BookingRequestState.Idle
    }

        fun initiateMpesaPayment(bookingId: Long, amountText: String, phoneText: String) {
        val amount = amountText.trim().toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _mpesaState.value = MpesaPaymentState.Failed("Enter a valid amount first")
            return
        }
        val phone = darajaRepository.normalizePhoneNumber(phoneText)
        if (phone == null) {
            _mpesaState.value = MpesaPaymentState.Failed("Enter a valid phone number (e.g. 0712345678)")
            return
        }
        viewModelScope.launch {
            _mpesaState.value = MpesaPaymentState.Requesting
            when (val outcome = darajaRepository.initiateStkPush(amount, phone, "Booking$bookingId")) {
                is StkPushOutcome.Success -> {
                    _mpesaState.value = MpesaPaymentState.WaitingForCustomer
                    when (val status = darajaRepository.pollUntilResolved(outcome.checkoutRequestId)) {
                        is StkPushStatus.Paid -> _mpesaState.value = MpesaPaymentState.Paid
                        is StkPushStatus.Failed -> _mpesaState.value = MpesaPaymentState.Failed(status.message)
                        StkPushStatus.TimedOut -> _mpesaState.value =
                            MpesaPaymentState.Failed("No response from M-Pesa in time. Please try again.")
                        StkPushStatus.Pending -> _mpesaState.value =
                            MpesaPaymentState.Failed("Could not confirm payment status. Check your M-Pesa messages.")
                    }
                }
                is StkPushOutcome.Failed -> {
                    _mpesaState.value = MpesaPaymentState.Failed(outcome.message)
                }
            }
        }
    }

    fun resetMpesaState() {
        _mpesaState.value = MpesaPaymentState.Idle
    }
}

sealed class BookingRequestState {
    data object Idle : BookingRequestState()
    data object Saving : BookingRequestState()
    data object Saved : BookingRequestState()
    data class Error(val message: String) : BookingRequestState()
}

sealed class MpesaPaymentState {
    data object Idle : MpesaPaymentState()
    data object Requesting : MpesaPaymentState()
    data object WaitingForCustomer : MpesaPaymentState()
    data object Paid : MpesaPaymentState()
    data class Failed(val message: String) : MpesaPaymentState()
}