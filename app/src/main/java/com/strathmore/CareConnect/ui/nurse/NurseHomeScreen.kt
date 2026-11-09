package com.strathmore.CareConnect.ui.nurse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.data.local.dao.BookingWithNames
import com.strathmore.CareConnect.ui.components.AppLogo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NurseHomeScreen(
    nurseId: Long,
    onLogout: () -> Unit,
    onViewPatient: (patientId: Long) -> Unit,
    onAddNote: (bookingId: Long, patientId: Long) -> Unit,
    nurseViewModel: NurseViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel()
) {
    val ownRecord by nurseViewModel.ownNurseRecord.collectAsState()
    val bookings by bookingViewModel.bookings.collectAsState()
    val requestPaymentState by bookingViewModel.requestPaymentState.collectAsState()

    LaunchedEffect(nurseId) {
        nurseViewModel.loadOwnNurseRecord(nurseId)
        bookingViewModel.loadBookingsForNurse(nurseId)
    }

    val pendingBookings = bookings.filter { it.status == "PENDING" }
    val acceptedBookings = bookings.filter { it.status == "ACCEPTED" }
    val pastBookings = bookings.filter { it.status == "DECLINED" || it.status == "COMPLETED" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppLogo() },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Log out")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Text(
                "Nurse Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Available for bookings", fontWeight = FontWeight.Bold)
                        Text(
                            "Caregivers can only find and book you while this is on.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Switch(
                        checked = ownRecord?.available ?: false,
                        onCheckedChange = { nurseViewModel.setAvailable(nurseId, it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
                    Text("Pending Requests (${pendingBookings.size})", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (pendingBookings.isEmpty()) {
                    item {
                        EmptyStateCard("No pending requests right now.")
                    }
                }

                items(pendingBookings, key = { it.bookingId }) { booking ->
                    PendingBookingCard(
                        booking = booking,
                        onAccept = { bookingViewModel.respondToBooking(booking.bookingId, true) },
                        onDecline = { bookingViewModel.respondToBooking(booking.bookingId, false) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Accepted \u2014 Upcoming or In Progress (${acceptedBookings.size})", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (acceptedBookings.isEmpty()) {
                    item {
                        EmptyStateCard("No accepted bookings right now.")
                    }
                }

                items(acceptedBookings, key = { it.bookingId }) { booking ->
                    AcceptedBookingCard(
                        booking = booking,
                        onViewPatient = { onViewPatient(booking.patientId) },
                        onAddNote = { onAddNote(booking.bookingId, booking.patientId) },
                        onMarkCompleted = { bookingViewModel.markCompleted(booking.bookingId) }
                    )
                }

                if (pastBookings.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Past Requests", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    items(pastBookings, key = { it.bookingId }) { booking ->
                        PastBookingCard(
                            booking = booking,
                            requestPaymentState = requestPaymentState,
                            onRequestPayment = { bookingId, amountText ->
                                bookingViewModel.requestPayment(bookingId, amountText)
                            },
                            onResetRequestPaymentState = { bookingViewModel.resetRequestPaymentState() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PendingBookingCard(booking: BookingWithNames, onAccept: () -> Unit, onDecline: () -> Unit) {
    val formatter = remember { SimpleDateFormat("EEE, d MMM yyyy \u00B7 h:mm a", Locale.US) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(booking.patientName, fontWeight = FontWeight.Bold)
            Text(
                "Requested: ${formatter.format(Date(booking.requestedVisitTime))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (booking.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(booking.notes, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept, modifier = Modifier.weight(1f)) {
                    Text("Accept")
                }
                OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f)) {
                    Text("Decline")
                }
            }
        }
    }
}

@Composable
private fun AcceptedBookingCard(
    booking: BookingWithNames,
    onViewPatient: () -> Unit,
    onAddNote: () -> Unit,
    onMarkCompleted: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("EEE, d MMM yyyy \u00B7 h:mm a", Locale.US) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(booking.patientName, fontWeight = FontWeight.Bold)
            Text(
                formatter.format(Date(booking.requestedVisitTime)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onViewPatient, modifier = Modifier.weight(1f)) {
                    Text("View Patient", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(onClick = onAddNote, modifier = Modifier.weight(1f)) {
                    Text("Add Note", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onMarkCompleted, modifier = Modifier.fillMaxWidth()) {
                Text("Mark Visit Completed")
            }
        }
    }
}

@Composable
private fun PastBookingCard(
    booking: BookingWithNames,
    requestPaymentState: BookingRequestState,
    onRequestPayment: (bookingId: Long, amountText: String) -> Unit,
    onResetRequestPaymentState: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("EEE, d MMM yyyy \u00B7 h:mm a", Locale.US) }
    var showRequestDialog by remember { mutableStateOf(false) }

    LaunchedEffect(requestPaymentState) {
        if (requestPaymentState is BookingRequestState.Saved) {
            showRequestDialog = false
            onResetRequestPaymentState()
        }
    }

    val statusColor = when (booking.status) {
        "COMPLETED" -> MaterialTheme.colorScheme.primary
        "DECLINED" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(booking.patientName, fontWeight = FontWeight.Bold)
                    Text(
                        formatter.format(Date(booking.requestedVisitTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(booking.status, color = statusColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }

            if (booking.status == "COMPLETED") {
                Spacer(modifier = Modifier.height(10.dp))
                if (booking.requestedAmountKsh == null) {
                    OutlinedButton(onClick = { showRequestDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Request Payment")
                    }
                } else {
                    Text(
                        "Payment requested: KSh ${"%.2f".format(booking.requestedAmountKsh)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showRequestDialog) {
        RequestPaymentDialog(
            state = requestPaymentState,
            onRequest = { amountText -> onRequestPayment(booking.bookingId, amountText) },
            onDismiss = {
                showRequestDialog = false
                onResetRequestPaymentState()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestPaymentDialog(
    state: BookingRequestState,
    onRequest: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Payment") },
        text = {
            Column {
                Text(
                    "Enter the amount owed for this visit. The caregiver will see this amount in My Bookings and can pay via M-Pesa.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (KSh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state is BookingRequestState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onRequest(amountText) },
                enabled = state !is BookingRequestState.Saving
            ) {
                Text(if (state is BookingRequestState.Saving) "Sending..." else "Send Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}