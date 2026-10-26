package com.strathmore.CareConnect.ui.symptom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSymptomScreen(
    symptomId: Long,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    onPainGuide: () -> Unit,
    viewModel: SymptomHistoryViewModel = viewModel()
) {
    val editingSymptom by viewModel.editingSymptom.collectAsState()
    val editState by viewModel.editState.collectAsState()

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

    var hasInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(symptomId) {
        viewModel.loadSymptomForEdit(symptomId)
    }

    LaunchedEffect(editingSymptom) {
        if (!hasInitialized && editingSymptom != null) {
            val s = editingSymptom!!
            if (SYMPTOM_TYPES.contains(s.type)) {
                selectedType = s.type
            } else {
                selectedType = "Other"
                otherTypeText = s.type
            }
            rating = s.rating.toFloat()
            notes = s.notes
            weightText = s.weightKg?.toString() ?: ""
            systolicText = s.systolicBp?.toString() ?: ""
            diastolicText = s.diastolicBp?.toString() ?: ""
            medicationTaken = s.medicationTaken
            surveyNote = s.surveyNote ?: ""
            if (s.weightKg != null || s.systolicBp != null || s.medicationTaken != null || !s.surveyNote.isNullOrBlank()) {
                showVitals = true
            }
            hasInitialized = true
        }
    }

    LaunchedEffect(editState) {
        if (editState is SymptomEditState.Saved) {
            onSaved()
            viewModel.resetEditState()
        }
    }

    val effectiveType = if (selectedType == "Other") otherTypeText else selectedType

    Scaffold(
        topBar = {
            CareTopBar(onBack = onBack)
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
                "Edit Symptom",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                CareTextField(
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
                CareTextField(
                    value = otherTypeText,
                    onValueChange = { otherTypeText = it },
                    label = { Text("Describe the symptom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Severity: ${rating.toInt()} / 10",
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = rating,
                        onValueChange = { rating = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            }

            CareTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            TextButton(
                onClick = { showVitals = !showVitals },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showVitals) "Hide vitals (optional)" else "+ Add vitals (optional)")
            }

            if (showVitals) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CareTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Weight (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CareTextField(
                                value = systolicText,
                                onValueChange = { systolicText = it },
                                label = { Text("Blood Pressure:High number") },
                                supportingText = { Text("Systolic, e.g. 120") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            CareTextField(
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

                        CareTextField(
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
                    viewModel.updateSymptom(
                        symptomId,
                        effectiveType,
                        rating.toInt(),
                        notes,
                        weightText.trim().toDoubleOrNull(),
                        systolicText.trim().toIntOrNull(),
                        diastolicText.trim().toIntOrNull(),
                        medicationTaken,
                        surveyNote.trim().ifBlank { null }
                    )
                },
                enabled = editState !is SymptomEditState.Saving,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (editState is SymptomEditState.Saving) "Saving..." else "Save Changes")
            }

            if (editState is SymptomEditState.Error) {
                Text(
                    (editState as SymptomEditState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}