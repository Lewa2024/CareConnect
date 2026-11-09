package com.strathmore.CareConnect.ui.nurse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun MyBookingsScreen(
    patientId: Long,
    onBack: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    var paymentTarget by remember { mutableStateOf<BookingWithNames?>(null) }

    LaunchedEffect(patientId) {
        viewModel.loadBookingsForPatient(patientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppLogo() },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "My Bookings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (bookings.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No bookings yet. Find a nurse to request a visit.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(bookings, key = { it.bookingId }) { booking ->
                    MyBookingCard(
                        booking = booking,
                        onRecordPayment = { paymentTarget = booking }
                    )
                }
            }
        }
    }

    paymentTarget?.let { booking ->
        RecordPaymentDialog(
            booking = booking,
            viewModel = viewModel,
            onDismiss = { paymentTarget = null }
        )
    }
}

@Composable
private fun MyBookingCard(booking: BookingWithNames, onRecordPayment: () -> Unit) {
    val formatter = remember { SimpleDateFormat("EEE, d MMM yyyy \u00B7 h:mm a", Locale.US) }
    val statusColor = when (booking.status) {
        "ACCEPTED" -> MaterialTheme.colorScheme.primary
        "DECLINED" -> MaterialTheme.colorScheme.error
        "COMPLETED" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant // PENDING
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "${booking.nurseFirstName} ${booking.nurseLastName}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    booking.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                formatter.format(Date(booking.requestedVisitTime)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (booking.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(booking.notes, style = MaterialTheme.typography.bodyMedium)
            }

            if (booking.status == "COMPLETED") {
                Spacer(modifier = Modifier.height(10.dp))
                if (booking.paymentAmountKsh != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Paid: KSh ${"%.2f".format(booking.paymentAmountKsh)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "M-Pesa code: ${booking.paymentMpesaCode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onRecordPayment,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Record Payment")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordPaymentDialog(
    booking: BookingWithNames,
    viewModel: BookingViewModel,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var mpesaCode by remember { mutableStateOf("") }
    val paymentState by viewModel.paymentState.collectAsState()

    LaunchedEffect(paymentState) {
        if (paymentState is BookingRequestState.Saved) {
            viewModel.resetPaymentState()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter what you paid ${booking.nurseFirstName} ${booking.nurseLastName} via M-Pesa for this visit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> amountText = filterDecimalInput(input) },
                    label = { Text("Amount (KSh)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mpesaCode,
                    onValueChange = { mpesaCode = it.uppercase() },
                    label = { Text("M-Pesa confirmation code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (paymentState is BookingRequestState.Error) {
                    Text(
                        (paymentState as BookingRequestState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.recordPayment(booking.bookingId, amountText, mpesaCode) },
                enabled = paymentState !is BookingRequestState.Saving
            ) {
                Text(if (paymentState is BookingRequestState.Saving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun filterDecimalInput(input: String): String {
    return input.filterIndexed { index, c -> c.isDigit() || (c == '.' && !input.take(index).contains('.')) }
}