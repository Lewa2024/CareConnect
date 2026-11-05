package com.strathmore.CareConnect.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo
import java.text.SimpleDateFormat
import java.util.Locale

private val SEX_OPTIONS = listOf("Male", "Female", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientScreen(
    caregiverId: Long,
    viewModel: PatientViewModel = viewModel(),
    onPatientAdded: (Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dateOfBirthText by remember { mutableStateOf("") } // format: YYYY-MM-DD
    var sex by remember { mutableStateOf("") }
    var sexDropdownExpanded by remember { mutableStateOf(false) }

    var chiefComplaint by remember { mutableStateOf("") }
    var historyOfIllness by remember { mutableStateOf("") }
    var pastMedicalHistory by remember { mutableStateOf("") }
    var surgicalHistory by remember { mutableStateOf("") }
    var familyHistory by remember { mutableStateOf("") }
    var currentPlan by remember { mutableStateOf("") }

    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var systolicText by remember { mutableStateOf("") }
    var diastolicText by remember { mutableStateOf("") }

    var medicationName by remember { mutableStateOf("") }
    var medicationDosage by remember { mutableStateOf("") }
    var medicationTimes by remember { mutableStateOf(listOf(8 to 0)) }
    var timeIndexBeingEdited by remember { mutableStateOf<Int?>(null) }

    var dateError by remember { mutableStateOf<String?>(null) }

    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is PatientSaveState.Saved) {
            onPatientAdded((saveState as PatientSaveState.Saved).patientId)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { AppLogo() })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "Add Patient",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Tell us about the patient you're caring for.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Patient's full name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dateOfBirthText,
                onValueChange = { dateOfBirthText = it; dateError = null },
                label = { Text("Date of birth (YYYY-MM-DD)") },
                singleLine = true,
                isError = dateError != null,
                supportingText = { dateError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = sexDropdownExpanded,
                onExpandedChange = { sexDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = sex,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sex") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = sexDropdownExpanded,
                    onDismissRequest = { sexDropdownExpanded = false }
                ) {
                    SEX_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { sex = option; sexDropdownExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = heightText,
                onValueChange = { input -> heightText = filterDecimalInput(input) },
                label = { Text("Height (cm) — optional") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Medical History",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = chiefComplaint,
                onValueChange = { chiefComplaint = it },
                label = { Text("Chief Complaint") },
                modifier = Modifier.fillMaxWidth().height(80.dp)
            )
            OutlinedTextField(
                value = historyOfIllness,
                onValueChange = { historyOfIllness = it },
                label = { Text("History of Present Illness") },
                modifier = Modifier.fillMaxWidth().height(80.dp)
            )
            OutlinedTextField(
                value = pastMedicalHistory,
                onValueChange = { pastMedicalHistory = it },
                label = { Text("Past Medical History") },
                modifier = Modifier.fillMaxWidth().height(80.dp)
            )
            OutlinedTextField(
                value = surgicalHistory,
                onValueChange = { surgicalHistory = it },
                label = { Text("Surgical History") },
                modifier = Modifier.fillMaxWidth().height(80.dp)
            )
            OutlinedTextField(
                value = familyHistory,
                onValueChange = { familyHistory = it },
                label = { Text("Family History") },
                modifier = Modifier.fillMaxWidth().height(80.dp)
            )
            OutlinedTextField(
                value = currentPlan,
                onValueChange = { currentPlan = it },
                label = { Text("Current Plan") },
                modifier = Modifier.fillMaxWidth().height(80.dp)
            )

            Text(
                "Baseline vitals (optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { input -> weightText = filterDecimalInput(input) },
                        label = { Text("Weight (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = systolicText,
                            onValueChange = { input -> systolicText = filterIntegerInput(input) },
                            label = { Text("Blood Pressure: Higher number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = diastolicText,
                            onValueChange = { input -> diastolicText = filterIntegerInput(input) },
                            label = { Text("Blood Pressure: Lower number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Text(
                "Current Medication (optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "If entered, this is saved to Manage Medications with reminders set up automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = medicationName,
                        onValueChange = { medicationName = it },
                        label = { Text("Medication name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = medicationDosage,
                        onValueChange = { medicationDosage = it },
                        label = { Text("Dosage (e.g. 500mg, 1 tablet)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Times per day: ${medicationTimes.size}", fontWeight = FontWeight.Bold)
                        Row {
                            IconButton(onClick = { if (medicationTimes.size > 1) medicationTimes = medicationTimes.dropLast(1) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Remove a time")
                            }
                            IconButton(onClick = { if (medicationTimes.size < 6) medicationTimes = medicationTimes + (12 to 0) }) {
                                Icon(Icons.Default.Add, contentDescription = "Add a time")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    medicationTimes.forEachIndexed { index, (hour, minute) ->
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

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val millis = parseDateOrNull(dateOfBirthText)
                    if (millis == null) {
                        dateError = "Enter a valid date as YYYY-MM-DD"
                        return@Button
                    }
                    viewModel.addPatient(
                        caregiverId = caregiverId,
                        name = name,
                        dateOfBirthMillis = millis,
                        sex = sex,
                        chiefComplaint = chiefComplaint,
                        historyOfIllness = historyOfIllness,
                        pastMedicalHistory = pastMedicalHistory,
                        surgicalHistory = surgicalHistory,
                        familyHistory = familyHistory,
                        currentPlan = currentPlan,
                        heightCm = heightText.trim().toDoubleOrNull(),
                        baselineWeightKg = weightText.trim().toDoubleOrNull(),
                        baselineSystolicBp = systolicText.trim().toIntOrNull(),
                        baselineDiastolicBp = diastolicText.trim().toIntOrNull(),
                        medicationName = medicationName,
                        medicationDosage = medicationDosage,
                        medicationTimes = if (medicationName.isNotBlank()) medicationTimes else emptyList()
                    )
                },
                enabled = saveState !is PatientSaveState.Saving,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (saveState is PatientSaveState.Saving) "Saving..." else "Save Patient")
            }

            if (saveState is PatientSaveState.Error) {
                Text(
                    (saveState as PatientSaveState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    timeIndexBeingEdited?.let { index ->
        val (currentHour, currentMinute) = medicationTimes[index]
        val timePickerState = rememberTimePickerState(
            initialHour = currentHour,
            initialMinute = currentMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { timeIndexBeingEdited = null },
            confirmButton = {
                TextButton(onClick = {
                    medicationTimes = medicationTimes.toMutableList().also {
                        it[index] = timePickerState.hour to timePickerState.minute
                    }
                    timeIndexBeingEdited = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { timeIndexBeingEdited = null }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

private fun parseDateOrNull(text: String): Long? {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        format.isLenient = false
        format.parse(text.trim())?.time
    } catch (e: Exception) {
        null
    }
}

private fun filterDecimalInput(input: String): String {
    return input.filterIndexed { index, c -> c.isDigit() || (c == '.' && !input.take(index).contains('.')) }
}

private fun filterIntegerInput(input: String): String {
    return input.filter { it.isDigit() }
}