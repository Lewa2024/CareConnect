package com.strathmore.CareConnect.ui.symptom

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomLogScreen(
    caregiverId: Long,
    patientId: Long,
    onBackToHome: () -> Unit,
    viewModel: SymptomViewModel = viewModel()
) {
    var type by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5f) }
    var notes by remember { mutableStateOf("") }

    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(caregiverId) {
        viewModel.loadPatients(caregiverId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Symptoms") },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Symptom type (e.g. Fatigue, Nausea, Pain)") },
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Text("Severity: ${rating.toInt()} / 10")
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 1f..10f,
                    steps = 8
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Button(
                onClick = {
                    viewModel.logSymptom(patientId, type, rating.toInt(), notes)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = saveState !is SaveState.Saving
            ) {
                Text(if (saveState is SaveState.Saving) "Saving..." else "Save Symptom")
            }

            when (val state = saveState) {
                is SaveState.Saved -> Text(
                    "Symptom logged successfully.",
                    color = MaterialTheme.colorScheme.primary
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