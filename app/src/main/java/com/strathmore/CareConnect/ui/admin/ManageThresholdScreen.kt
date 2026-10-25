package com.strathmore.CareConnect.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.data.local.entities.RuleThreshold
import com.strathmore.CareConnect.ui.components.AppLogo
import com.strathmore.CareConnect.ui.symptom.SYMPTOM_TYPES

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageThresholdsScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val thresholds by viewModel.thresholds.collectAsState()
    var editingThreshold by remember { mutableStateOf<RuleThreshold?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var thresholdPendingDelete by remember { mutableStateOf<RuleThreshold?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadThresholds()
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add rule threshold")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Rule Thresholds",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            Text(
                "These rules feed Rule 1 (Absolute Threshold) of the alert engine. " +
                    "Symptom types without a rule here still use a safety-net default of severity \u2265 8, 1 entry.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (thresholds.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No rules configured yet. Tap + to add one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(thresholds, key = { it.ruleThresholdId }) { threshold ->
                        ThresholdCard(
                            threshold = threshold,
                            onEdit = { editingThreshold = threshold },
                            onDelete = { thresholdPendingDelete = threshold }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ThresholdEditorDialog(
            initial = null,
            availableTypes = viewModel.unconfiguredMetrics(),
            onDismiss = { showAddDialog = false },
            onSave = { metric, value, consecutive ->
                viewModel.saveThreshold(metric, value, consecutive)
                showAddDialog = false
            }
        )
    }

    editingThreshold?.let { threshold ->
        ThresholdEditorDialog(
            initial = threshold,
            availableTypes = SYMPTOM_TYPES.filter { it != "Other" },
            onDismiss = { editingThreshold = null },
            onSave = { metric, value, consecutive ->
                viewModel.saveThreshold(metric, value, consecutive)
                editingThreshold = null
            }
        )
    }

    thresholdPendingDelete?.let { threshold ->
        AlertDialog(
            onDismissRequest = { thresholdPendingDelete = null },
            title = { Text("Remove this rule?") },
            text = { Text("${threshold.metric} will fall back to the default safety-net rule (severity \u2265 8, 1 entry) instead.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteThreshold(threshold)
                    thresholdPendingDelete = null
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { thresholdPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThresholdCard(threshold: RuleThreshold, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(threshold.metric, fontWeight = FontWeight.Bold)
                Text(
                    "Severity \u2265 ${threshold.thresholdValue}/10, for ${threshold.consecutiveEntriesRequired} consecutive entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThresholdEditorDialog(
    initial: RuleThreshold?,
    availableTypes: List<String>,
    onDismiss: () -> Unit,
    onSave: (metric: String, thresholdValue: Int, consecutiveEntriesRequired: Int) -> Unit
) {
    var metric by remember { mutableStateOf(initial?.metric ?: availableTypes.firstOrNull() ?: "") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var severity by remember { mutableStateOf((initial?.thresholdValue ?: 8).toFloat()) }
    var consecutiveText by remember { mutableStateOf((initial?.consecutiveEntriesRequired ?: 1).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Rule Threshold" else "Edit Rule Threshold") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { if (initial == null) dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = metric,
                        onValueChange = {},
                        readOnly = true,
                        enabled = initial == null,
                        label = { Text("Symptom type") },
                        trailingIcon = {
                            if (initial == null) ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    if (initial == null) {
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            availableTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = { metric = type; dropdownExpanded = false }
                                )
                            }
                        }
                    }
                }

                Column {
                    Text("Severity threshold: ${severity.toInt()}/10")
                    Slider(value = severity, onValueChange = { severity = it }, valueRange = 1f..10f, steps = 8)
                }

                OutlinedTextField(
                    value = consecutiveText,
                    onValueChange = { consecutiveText = it },
                    label = { Text("Consecutive entries required") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(metric, severity.toInt(), consecutiveText.trim().toIntOrNull() ?: 1)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}