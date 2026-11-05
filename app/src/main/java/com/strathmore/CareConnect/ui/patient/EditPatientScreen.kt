package com.strathmore.CareConnect.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SEX_OPTIONS = listOf("Male", "Female", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPatientScreen(
    patientId: Long,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: PatientViewModel = viewModel()
) {
    val editingPatient by viewModel.editingPatient.collectAsState()
    val editState by viewModel.editState.collectAsState()

    var name by remember { mutableStateOf("") }
    var dateOfBirthText by remember { mutableStateOf("") }
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

    var dateError by remember { mutableStateOf<String?>(null) }
    var hasInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        viewModel.loadPatientForEdit(patientId)
    }

    LaunchedEffect(editingPatient) {
        if (!hasInitialized && editingPatient != null) {
            val p = editingPatient!!
            name = p.name
            dateOfBirthText = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(p.dateOfBirth))
            sex = p.sex
            chiefComplaint = p.chiefComplaint
            historyOfIllness = p.historyOfIllness
            pastMedicalHistory = p.pastMedicalHistory
            surgicalHistory = p.surgicalHistory
            familyHistory = p.familyHistory
            currentPlan = p.currentPlan
            heightText = p.heightCm?.toString() ?: ""
            weightText = p.baselineWeightKg?.toString() ?: ""
            systolicText = p.baselineSystolicBp?.toString() ?: ""
            diastolicText = p.baselineDiastolicBp?.toString() ?: ""
            hasInitialized = true
        }
    }

    LaunchedEffect(editState) {
        if (editState is EditState.Saved) {
            onSaved()
            viewModel.resetEditState()
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "Edit Patient",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
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
                onValueChange = { heightText = it },
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
                "Baseline vitals",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                    label = { Text("BP: Higher number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = diastolicText,
                    onValueChange = { diastolicText = it },
                    label = { Text("BP: Lower number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val millis = parseDateOrNull(dateOfBirthText)
                    if (millis == null) {
                        dateError = "Enter a valid date as YYYY-MM-DD"
                        return@Button
                    }
                    viewModel.updatePatient(
                        patientId = patientId,
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
                        baselineDiastolicBp = diastolicText.trim().toIntOrNull()
                    )
                },
                enabled = editState !is EditState.Saving,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (editState is EditState.Saving) "Saving..." else "Save Changes")
            }

            if (editState is EditState.Error) {
                Text(
                    (editState as EditState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
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