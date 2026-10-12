package com.strathmore.CareConnect.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var medicalHistory by remember { mutableStateOf("") }
    var medication by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }
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
            medicalHistory = p.medicalHistory
            medication = p.medication
            heightText = p.heightCm?.toString() ?: ""
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
        .verticalScroll(rememberScrollState())   // <-- added
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

            OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = { Text("Height (cm) — optional") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = medicalHistory,
                onValueChange = { medicalHistory = it },
                label = { Text("Medical history") },
                modifier = Modifier.fillMaxWidth().height(110.dp)
            )

            OutlinedTextField(
                value = medication,
                onValueChange = { medication = it },
                label = { Text("Current medication") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val millis = parseDateOrNull(dateOfBirthText)
                    if (millis == null) {
                        dateError = "Enter a valid date as YYYY-MM-DD"
                        return@Button
                    }
                    val height = heightText.trim().toDoubleOrNull()
                    viewModel.updatePatient(patientId, name, millis, medicalHistory, medication, height)
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
