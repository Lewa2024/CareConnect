package com.strathmore.CareConnect.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AddPatientScreen(
    caregiverId: Long,
    viewModel: PatientViewModel = viewModel(),
    onPatientAdded: (Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dateOfBirthText by remember { mutableStateOf("") } // format: YYYY-MM-DD
    var medicalHistory by remember { mutableStateOf("") }
    var medication by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf<String?>(null) }

    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is PatientSaveState.Saved) {
            onPatientAdded((saveState as PatientSaveState.Saved).patientId)
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add Patient", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Patient's full name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dateOfBirthText,
            onValueChange = { dateOfBirthText = it; dateError = null },
            label = { Text("Date of birth (YYYY-MM-DD)") },
            isError = dateError != null,
            supportingText = { dateError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = medicalHistory,
            onValueChange = { medicalHistory = it },
            label = { Text("Medical history") },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )

        OutlinedTextField(
            value = medication,
            onValueChange = { medication = it },
            label = { Text("Current medication") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val millis = parseDateOrNull(dateOfBirthText)
                if (millis == null) {
                    dateError = "Enter a valid date as YYYY-MM-DD"
                    return@Button
                }
                viewModel.addPatient(caregiverId, name, millis, medicalHistory, medication)
            },
            enabled = saveState !is PatientSaveState.Saving,
            modifier = Modifier.fillMaxWidth()
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

private fun parseDateOrNull(text: String): Long? {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        format.isLenient = false
        format.parse(text.trim())?.time
    } catch (e: Exception) {
        null
    }
}