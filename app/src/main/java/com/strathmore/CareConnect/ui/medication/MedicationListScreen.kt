package com.strathmore.CareConnect.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.data.local.entities.Medication
import com.strathmore.CareConnect.data.local.entities.MedicationTime
import com.strathmore.CareConnect.ui.components.AppLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    patientId: Long,
    onBack: () -> Unit,
    onAddMedication: () -> Unit,
    viewModel: MedicationViewModel = viewModel()
) {
    val medications by viewModel.medications.collectAsState()
    var medicationPendingStop by remember { mutableStateOf<Medication?>(null) }

    LaunchedEffect(patientId) {
        viewModel.loadMedications(patientId)
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedication) {
                Icon(Icons.Default.Add, contentDescription = "Add medication")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Medications",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            if (medications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No medications added yet. Tap + to add one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(medications, key = { it.medicationId }) { medication ->
                        MedicationCard(
                            medication = medication,
                            onStop = { medicationPendingStop = medication }
                        )
                    }
                }
            }
        }
    }

    medicationPendingStop?.let { medication ->
        AlertDialog(
            onDismissRequest = { medicationPendingStop = null },
            title = { Text("Stop this medication?") },
            text = { Text("Reminders for ${medication.name} will be cancelled. This can't be undone from here.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.stopMedication(medication.medicationId)
                    medicationPendingStop = null
                }) {
                    Text("Stop", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { medicationPendingStop = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    onStop: () -> Unit,
    viewModel: MedicationViewModel = viewModel()
) {
    val times by viewModel.getTimesFlowFor(medication.medicationId).collectAsState(initial = emptyList())

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
                Column {
                    Text(medication.name, fontWeight = FontWeight.Bold)
                    Text(
                        medication.dosage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onStop) {
                    Icon(Icons.Default.Delete, contentDescription = "Stop medication", tint = MaterialTheme.colorScheme.error)
                }
            }

            if (times.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    times.joinToString("  \u2022  ") { "%02d:%02d".format(it.hour, it.minute) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (medication.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(medication.notes, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}