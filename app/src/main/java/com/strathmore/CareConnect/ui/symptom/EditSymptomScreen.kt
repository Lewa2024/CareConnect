package com.strathmore.CareConnect.ui.symptom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun EditSymptomScreen(
    symptomId: Long,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: SymptomHistoryViewModel = viewModel()
) {
    val editingSymptom by viewModel.editingSymptom.collectAsState()
    val editState by viewModel.editState.collectAsState()

    var selectedType by remember { mutableStateOf("") }
    var otherTypeText by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(5f) }
    var notes by remember { mutableStateOf("") }
    var hasInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(symptomId) {
        viewModel.loadSymptomForEdit(symptomId)
    }

    // Seed the fields once the symptom loads, but only the first time —
    // so we don't overwrite what the user is typing on every recomposition.
    LaunchedEffect(editingSymptom) {
        if (!hasInitialized && editingSymptom != null) {
            val s = editingSymptom!!
            if (SYMPTOM_TYPES.contains(s.type)) {
                selectedType = s.type
            } else {
                selectedType = "Other"
                otherTypeText = s.type
            }
            rating = s.rating.toFloat()
            notes = s.notes
            hasInitialized = true
        }
    }

    LaunchedEffect(editState) {
        if (editState is SymptomEditState.Saved) {
            onSaved()
            viewModel.resetEditState()
        }
    }

    val effectiveType = if (selectedType == "Other") otherTypeText else selectedType

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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "Edit Symptom",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Symptom type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    SYMPTOM_TYPES.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedType = option
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            if (selectedType == "Other") {
                OutlinedTextField(
                    value = otherTypeText,
                    onValueChange = { otherTypeText = it },
                    label = { Text("Describe the symptom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Severity: ${rating.toInt()} / 10",
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = rating,
                        onValueChange = { rating = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Button(
                onClick = { viewModel.updateSymptom(symptomId, effectiveType, rating.toInt(), notes) },
                enabled = editState !is SymptomEditState.Saving,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (editState is SymptomEditState.Saving) "Saving..." else "Save Changes")
            }

            if (editState is SymptomEditState.Error) {
                Text(
                    (editState as SymptomEditState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}