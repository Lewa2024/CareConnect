package com.strathmore.CareConnect.ui.nurse

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
fun AddPostVisitNoteScreen(
    bookingId: Long,
    patientId: Long,
    nurseId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    var noteText by remember { mutableStateOf("") }
    val saveState by viewModel.noteSaveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is BookingRequestState.Saved) {
            onSaved()
            viewModel.resetNoteSaveState()
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
                "Post-Visit Note",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Share what you observed during the visit. This will be visible to the patient's caregiver.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Visit notes") },
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )

            Button(
                onClick = { viewModel.addPostVisitNote(bookingId, patientId, nurseId, noteText) },
                enabled = saveState !is BookingRequestState.Saving,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (saveState is BookingRequestState.Saving) "Saving..." else "Save Note")
            }

            if (saveState is BookingRequestState.Error) {
                Text(
                    (saveState as BookingRequestState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}