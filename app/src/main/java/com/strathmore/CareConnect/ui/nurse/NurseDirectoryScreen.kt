package com.strathmore.CareConnect.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.data.local.dao.NurseDirectoryEntry
import com.strathmore.CareConnect.ui.components.AppLogo

private enum class NurseActionKind { VERIFY, ACTIVE }

private data class PendingNurseAction(val nurse: NurseDirectoryEntry, val kind: NurseActionKind)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NurseDirectoryScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val nurses by viewModel.nurses.collectAsState()
    var pendingAction by remember { mutableStateOf<PendingNurseAction?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadNurseDirectory()
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
                Icon(Icons.Default.LocalHospital, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Nurses",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (nurses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No nurses registered yet.",
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
                    items(nurses, key = { it.nurseId }) { nurse ->
                        NurseCard(
                            nurse = nurse,
                            onToggleVerified = { pendingAction = PendingNurseAction(nurse, NurseActionKind.VERIFY) },
                            onToggleActive = { pendingAction = PendingNurseAction(nurse, NurseActionKind.ACTIVE) }
                        )
                    }
                }
            }
        }
    }

    pendingAction?.let { action ->
        val nurse = action.nurse
        val name = "${nurse.firstName} ${nurse.lastName}"
        val isVerify = action.kind == NurseActionKind.VERIFY

        // The change being asked for, worked out once so the title, message and button agree.
        val title: String
        val message: String
        val confirmLabel: String
        val isDestructive: Boolean
        if (isVerify) {
            if (nurse.verified) {
                title = "Remove verification?"
                message = "$name will no longer appear when caregivers look for a nurse."
                confirmLabel = "Unverify"
                isDestructive = true
            } else {
                title = "Verify this nurse?"
                message = "$name will appear when caregivers look for a nurse, as long as the account is active."
                confirmLabel = "Verify"
                isDestructive = false
            }
        } else {
            if (nurse.active) {
                title = "Deactivate this account?"
                message = "$name will no longer be able to log in or be found by caregivers. Their data is kept, not deleted."
                confirmLabel = "Deactivate"
                isDestructive = true
            } else {
                title = "Reactivate this account?"
                message = "$name will be able to log in again."
                confirmLabel = "Reactivate"
                isDestructive = false
            }
        }

        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    if (isVerify) {
                        viewModel.setNurseVerified(nurse.nurseId, !nurse.verified)
                    } else {
                        viewModel.setNurseActive(nurse.userId, !nurse.active)
                    }
                    pendingAction = null
                }) {
                    Text(
                        confirmLabel,
                        color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
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
private fun StatusChip(text: String, positive: Boolean) {
    val color = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text,
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun NurseCard(
    nurse: NurseDirectoryEntry,
    onToggleVerified: () -> Unit,
    onToggleActive: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column {
                Text("${nurse.firstName} ${nurse.lastName}", fontWeight = FontWeight.Bold)
                Text(
                    nurse.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(if (nurse.verified) "Verified" else "Unverified", nurse.verified)
                StatusChip(if (nurse.active) "Active" else "Deactivated", nurse.active)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${nurse.qualification} \u00B7 ${nurse.city ?: "No location"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${nurse.phoneNumber} \u00B7 ${nurse.bookingCount} booking${if (nurse.bookingCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onToggleVerified, modifier = Modifier.weight(1f)) {
                    Text(if (nurse.verified) "Unverify" else "Verify")
                }
                OutlinedButton(onClick = onToggleActive, modifier = Modifier.weight(1f)) {
                    Text(if (nurse.active) "Deactivate" else "Reactivate")
                }
            }
        }
    }
}