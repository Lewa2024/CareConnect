package com.strathmore.CareConnect.ui.symptom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Info
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomLogScreen(
    caregiverId: Long,
    patientId: Long,
    onBackToHome: () -> Unit,
    onPainGuide: () -> Unit,
    viewModel: SymptomViewModel = viewModel()
) {
    var selectedType by remember { mutableStateOf("") }
    var otherTypeText by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(5f) }
    var notes by remember { mutableStateOf("") }

    var showVitals by remember { mutableStateOf(false) }
    var weightText by remember { mutableStateOf("") }
    var systolicText by remember { mutableStateOf("") }
    var diastolicText by remember { mutableStateOf("") }
    var medicationTaken by remember { mutableStateOf<Boolean?>(null) }
    var surveyNote by remember { mutableStateOf("") }

    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(caregiverId) {
        viewModel.loadPatients(caregiverId)
    }

    val effectiveType = if (selectedType == "Other") otherTypeText else selectedType

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppLogo() },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Home")
                    }
                }
            )
        }
    ) { padding ->
        Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())   // <-- added
        .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
) {
            Text(
                "Log Symptoms",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Symptom type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    SYMPTOM_TYPES.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedType = option
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            if (selectedType == "Other") {
                OutlinedTextField(
                    value = otherTypeText,
                    onValueChange = { otherTypeText = it },
                    label = { Text("Describe the symptom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                            "Severity: ${rating.toInt()} / 10",
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onPainGuide) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guide", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Slider(
                        value = rating,
                        onValueChange = { rating = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            // Optional vitals, collapsed by default to keep the core form short.
            TextButton(
                onClick = { showVitals = !showVitals },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showVitals) "Hide vitals (optional)" else "+ Add vitals (optional)")
            }

            if (showVitals) {
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
                            onValueChange = { weightText = it },
                            label = { Text("Weight (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = systolicText,
                                onValueChange = { systolicText = it },
                                label = { Text("Blood Pressure:High number") },
                                supportingText = { Text("Systolic, e.g. 120") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = diastolicText,
                                onValueChange = { diastolicText = it },
                                label = { Text("Blood Pressure:Low number") },
                                supportingText = { Text("Diastolic, e.g. 80") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Medication taken today?")
                            Switch(
                                checked = medicationTaken == true,
                                onCheckedChange = { medicationTaken = it }
                            )
                        }

                        OutlinedTextField(
                            value = surveyNote,
                            onValueChange = { surveyNote = it },
                            label = { Text("Quick wellbeing note (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.logSymptom(
                        patientId = patientId,
                        type = effectiveType,
                        rating = rating.toInt(),
                        notes = notes,
                        weightKg = weightText.trim().toDoubleOrNull(),
                        systolicBp = systolicText.trim().toIntOrNull(),
                        diastolicBp = diastolicText.trim().toIntOrNull(),
                        medicationTaken = medicationTaken,
                        surveyNote = surveyNote.trim().ifBlank { null }
                    )
                },
                enabled = saveState !is SaveState.Saving,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (saveState is SaveState.Saving) "Saving..." else "Save Symptom")
            }

            when (val state = saveState) {
                is SaveState.Saved -> Text(
                    "Symptom logged successfully.",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                is SaveState.Error -> Text(
                    state.message,
                    color = MaterialTheme.colorScheme.error
                )
                else -> {}
            }
        }
    }
}