package com.strathmore.CareConnect.ui.nurse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo
import com.strathmore.CareConnect.ui.medication.MedicationViewModel
import com.strathmore.CareConnect.ui.patient.PatientViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NursePatientViewScreen(
    nurseId: Long,
    patientId: Long,
    onBack: () -> Unit,
    bookingViewModel: BookingViewModel = viewModel(),
    patientViewModel: PatientViewModel = viewModel(),
    medicationViewModel: MedicationViewModel = viewModel()
) {
    val hasAccess by bookingViewModel.hasAccess.collectAsState()
    val patient by patientViewModel.viewingPatient.collectAsState()
    val medications by medicationViewModel.medications.collectAsState()

    LaunchedEffect(nurseId, patientId) {
        bookingViewModel.checkAccess(nurseId, patientId)
        patientViewModel.loadPatientDetails(patientId)
        medicationViewModel.loadMedications(patientId)
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
        if (!hasAccess) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "You can only view a patient's details once you've accepted a booking for them.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (patient == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val p = patient!!
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

                Text("Current Medications", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (medications.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "No medications recorded.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    medications.forEach { medication ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(medication.name, fontWeight = FontWeight.Bold)
                                Text(
                                    medication.dosage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (medication.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(medication.notes, style = MaterialTheme.typography.bodySmall)
                                }
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

private fun calculateAge(dateOfBirthMillis: Long): Int {
    val dob = Calendar.getInstance().apply { timeInMillis = dateOfBirthMillis }
    val today = Calendar.getInstance()
    var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--
    return age
}