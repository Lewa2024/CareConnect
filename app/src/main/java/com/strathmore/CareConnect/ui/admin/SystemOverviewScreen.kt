package com.strathmore.CareConnect.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemOverviewScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSystemStats()
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
        val s = stats
        if (s == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "System Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Aggregate usage across the whole system. No individual patient data is shown here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        icon = Icons.Default.People,
                        label = "Caregivers",
                        value = s.totalCaregivers.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.PersonPin,
                        label = "Patients",
                        value = s.totalPatients.toString(),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        icon = Icons.Default.Assignment,
                        label = "Active Symptom Entries",
                        value = s.activeSymptomEntries.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.DeleteSweep,
                        label = "Deleted Entries",
                        value = s.deletedSymptomEntries.toString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Rule Engine Activity", fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        icon = Icons.Default.Warning,
                        label = "Urgent Alerts",
                        value = s.urgentAlerts.toString(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.Info,
                        label = "Routine Alerts",
                        value = s.routineAlerts.toString(),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                val total = s.urgentAlerts + s.routineAlerts
                if (total > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "${(s.urgentAlerts * 100 / total)}% of all alerts generated so far have been URGENT.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}