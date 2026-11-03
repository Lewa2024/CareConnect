package com.strathmore.CareConnect.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.data.local.dao.AdminDeletionLogEntry
import com.strathmore.CareConnect.ui.components.AppLogo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletedSymptomsScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val deletionLog by viewModel.deletionLog.collectAsState()
    var pendingRestore by remember { mutableStateOf<AdminDeletionLogEntry?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadDeletionLog()
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Deletion Audit Log",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            Text(
                "Symptom entries caregivers have removed. Shown here: which patient and " +
                    "what type of symptom, so you can restore one if needed \u2014 severity and " +
                    "notes are never shown to protect patient privacy.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (deletionLog.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No deletions recorded yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(deletionLog, key = { it.symptomId }) { entry ->
                        DeletionLogCard(
                            entry = entry,
                            onRestoreClick = { pendingRestore = entry }
                        )
                    }
                }
            }
        }
    }

    pendingRestore?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("Restore this entry?") },
            text = {
                Text(
                    "This will restore the \"${entry.type}\" entry for ${entry.patientName} " +
                        "back into their active symptom history."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restoreSymptom(entry.symptomId)
                    pendingRestore = null
                }) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DeletionLogCard(entry: AdminDeletionLogEntry, onRestoreClick: () -> Unit) {
    val formatter = remember { SimpleDateFormat("EEE, d MMM yyyy \u00B7 h:mm a", Locale.US) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("${entry.type} \u2014 ${entry.patientName}", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onRestoreClick) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restore")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Logged: ${formatter.format(Date(entry.loggedAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            entry.deletedAt?.let {
                Text(
                    "Deleted: ${formatter.format(Date(it))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}