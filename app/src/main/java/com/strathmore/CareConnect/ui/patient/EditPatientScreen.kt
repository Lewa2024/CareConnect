package com.strathmore.CareConnect.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo

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
    var hasInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        viewModel.loadPatientForEdit(patientId)
    }

    // Seed the text field once the patient loads, but only the first time —
    // so we don't overwrite what the user is typing on every recomposition.
    LaunchedEffect(editingPatient) {
        if (!hasInitialized && editingPatient != null) {
            name = editingPatient!!.name
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Edit Patient Name",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Patient's full name") },
                singleLine = true,
                isError = editState is EditState.Error,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.updatePatientName(patientId, name) },
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