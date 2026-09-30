package com.strathmore.CareConnect.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    patientId: Long,
    onBack: () -> Unit,
    onEdit: (patientId: Long) -> Unit,
    viewModel: PatientViewModel = viewModel()
) {
    val patient by viewModel.viewingPatient.collectAsState()

    LaunchedEffect(patientId) {
        viewModel.loadPatientDetails(patientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppLogo() },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(patientId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit patient")
                    }
                }
            )
        }
    ) { padding ->
        if (patient == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val p = patient!!
            val dobFormatter = remember { SimpleDateFormat("d MMMM yyyy", Locale.US) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    p.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                DetailCard(label = "Date of Birth", value = dobFormatter.format(Date(p.dateOfBirth)))
                DetailCard(label = "Medical History", value = p.medicalHistory.ifBlank { "None recorded" })
                DetailCard(label = "Current Medication", value = p.medication.ifBlank { "None recorded" })

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onEdit(patientId) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Patient Details")
                }
            }
        }
    }
}

@Composable
private fun DetailCard(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}