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
import java.text.SimpleDateFormat
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
            dateOfBirthText = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date(p.dateOfBirth))
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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