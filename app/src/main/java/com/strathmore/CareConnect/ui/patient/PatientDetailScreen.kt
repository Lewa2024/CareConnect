package com.strathmore.CareConnect.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo
import com.strathmore.CareConnect.ui.nurse.BookingViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    patientId: Long,
    onBack: () -> Unit,
    onEdit: (patientId: Long) -> Unit,
    onViewMedications: (patientId: Long) -> Unit,
    viewModel: PatientViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel()
) {
    val patient by viewModel.viewingPatient.collectAsState()
    val notes by bookingViewModel.notes.collectAsState()

    LaunchedEffect(patientId) {
        viewModel.loadPatientDetails(patientId)
        bookingViewModel.loadNotesForPatient(patientId)
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
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val p = patient!!
            val noteFormatter = remember { SimpleDateFormat("EEE, d MMM yyyy \u00B7 h:mm a", Locale.US) }
            val age = remember(p.dateOfBirth) { calculateAge(p.dateOfBirth) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(p.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                Text("Medical History", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        TableRow("Patient Name", p.name)
                        TableRow("Age", "$age years")
                        TableRow("Sex", p.sex.ifBlank { "Not recorded" })
                        TableRow("Chief Complaint", p.chiefComplaint.ifBlank { "Not recorded" })
                        TableRow("History of Illness", p.historyOfIllness.ifBlank { "Not recorded" })
                        TableRow("Past Medical History", p.pastMedicalHistory.ifBlank { "Not recorded" })
                        TableRow("Surgical History", p.surgicalHistory.ifBlank { "Not recorded" })
                        TableRow("Family History", p.familyHistory.ifBlank { "Not recorded" })
                        TableRow("Current Plan", p.currentPlan.ifBlank { "Not recorded" }, isLast = true)
                    }
                }

                Text("Baseline Vitals", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                DetailCard(label = "Height", value = p.heightCm?.let { "$it cm" } ?: "Not recorded")
                DetailCard(label = "Baseline Weight", value = p.baselineWeightKg?.let { "$it kg" } ?: "Not recorded")
                DetailCard(
                    label = "Baseline Blood Pressure",
                    value = if (p.baselineSystolicBp != null && p.baselineDiastolicBp != null)
                        "${p.baselineSystolicBp}/${p.baselineDiastolicBp}"
                    else "Not recorded"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onViewMedications(patientId) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Medications")
                }

                OutlinedButton(
                    onClick = { onEdit(patientId) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Patient Details")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Nurse Visit Notes", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                if (notes.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "No nurse notes yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    notes.forEach { note ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "${note.nurseFirstName} ${note.nurseLastName}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    noteFormatter.format(Date(note.createdAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(note.note, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableRow(label: String, value: String, isLast: Boolean = false) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
    if (!isLast) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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

private fun calculateAge(dateOfBirthMillis: Long): Int {
    val dob = Calendar.getInstance().apply { timeInMillis = dateOfBirthMillis }
    val today = Calendar.getInstance()
    var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--
    return age
}