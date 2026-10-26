package com.strathmore.CareConnect.ui.medication

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    patientId: Long,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: MedicationViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var times by remember { mutableStateOf(listOf(8 to 0)) } // default: one dose at 8:00
    var timeIndexBeingEdited by remember { mutableStateOf<Int?>(null) }

    val saveState by viewModel.saveState.collectAsState()

        val context = LocalContext.current
    var notificationsDenied by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsDenied = !granted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!alreadyGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(saveState) {
        if (saveState is MedicationSaveState.Saved) {
            onSaved()
            viewModel.resetSaveState()
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        Text(
                "Add Medication",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
                       Text(
                "Set up a daily reminder so you never miss a dose.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (notificationsDenied) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Notifications are turned off, so reminders won't appear. " +
                            "You can still save this medication, and enable notifications later in your phone's Settings.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medication name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage (e.g. 500mg, 1 tablet)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth().height(90.dp)
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Times per day: ${times.size}", fontWeight = FontWeight.Bold)
                        Row {
                            IconButton(
                                onClick = { if (times.size > 1) times = times.dropLast(1) }
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Remove a time")
                            }
                            IconButton(
                                onClick = { if (times.size < 6) times = times + (12 to 0) }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add a time")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    times.forEachIndexed { index, (hour, minute) ->
                        OutlinedButton(
                            onClick = { timeIndexBeingEdited = index },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dose ${index + 1}: ${"%02d".format(hour)}:${"%02d".format(minute)}")
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.addMedication(patientId, name, dosage, notes, times)
                },
                enabled = saveState !is MedicationSaveState.Saving,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (saveState is MedicationSaveState.Saving) "Saving..." else "Save Medication")
            }

            if (saveState is MedicationSaveState.Error) {
                Text(
                    (saveState as MedicationSaveState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    timeIndexBeingEdited?.let { index ->
        val (currentHour, currentMinute) = times[index]
        val timePickerState = rememberTimePickerState(
            initialHour = currentHour,
            initialMinute = currentMinute,
            is24Hour = true
        )
        TimePickerDialog(
            onDismiss = { timeIndexBeingEdited = null },
            onConfirm = {
                times = times.toMutableList().also { it[index] = timePickerState.hour to timePickerState.minute }
                timeIndexBeingEdited = null
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { content() }
    )
}