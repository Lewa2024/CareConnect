package com.strathmore.CareConnect.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.data.local.dao.CaregiverDirectoryEntry
import com.strathmore.CareConnect.ui.components.AppLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDirectoryScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val caregivers by viewModel.caregivers.collectAsState()
    var pendingAction by remember { mutableStateOf<CaregiverDirectoryEntry?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadCaregiverDirectory()
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Icon(Icons.Default.People, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Caregivers",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (caregivers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No caregivers registered yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(caregivers, key = { it.userId }) { caregiver ->
                        CaregiverCard(
                            caregiver = caregiver,
                            onToggleActive = { pendingAction = caregiver }
                        )
                    }
                }
            }
        }
    }

    pendingAction?.let { caregiver ->
        val willDeactivate = caregiver.active
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(if (willDeactivate) "Deactivate this account?" else "Reactivate this account?") },
            text = {
                Text(
                    if (willDeactivate)
                        "${caregiver.firstName} ${caregiver.lastName} will no longer be able to log in. Their data is kept, not deleted."
                    else
                        "${caregiver.firstName} ${caregiver.lastName} will be able to log in again."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setCaregiverActive(caregiver.userId, !willDeactivate)
                    pendingAction = null
                }) {
                    Text(
                        if (willDeactivate) "Deactivate" else "Reactivate",
                        color = if (willDeactivate) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CaregiverCard(caregiver: CaregiverDirectoryEntry, onToggleActive: () -> Unit) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text("${caregiver.firstName} ${caregiver.lastName}", fontWeight = FontWeight.Bold)
                    Text(
                        caregiver.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (caregiver.active)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Text(
                        if (caregiver.active) "Active" else "Deactivated",
                        color = if (caregiver.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "${caregiver.phoneNumber} \u00B7 ${caregiver.patientCount} patient${if (caregiver.patientCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onToggleActive,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (caregiver.active) "Deactivate" else "Reactivate")
            }
        }
    }
}