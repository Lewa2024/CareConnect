package com.strathmore.CareConnect.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.data.local.entities.Notification
import com.strathmore.CareConnect.ui.components.AppLogo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    patientId: Long,
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    onComingSoon: (String) -> Unit,
    viewModel: AlertsViewModel = viewModel()
) {
    val alerts by viewModel.alerts.collectAsState()

    LaunchedEffect(patientId) {
        viewModel.loadAlerts(patientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { AppLogo() })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onHistory,
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onComingSoon("Nurse booking") },
                    icon = { Icon(Icons.Default.LocalHospital, contentDescription = "Book") },
                    label = { Text("Book") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* already here */ },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                    label = { Text("Alerts") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Alerts",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            if (alerts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No alerts yet. The rule engine checks every symptom you log.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(alerts, key = { it.notificationId }) { alert ->
                        AlertCard(alert, onClick = { viewModel.markAsRead(alert.notificationId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: Notification, onClick: () -> Unit) {
    val isUrgent = alert.urgency == "URGENT"
    val accentColor = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    val formatter = remember { SimpleDateFormat("EEE, d MMM \u00B7 h:mm a", Locale.US) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (alert.isRead) MaterialTheme.colorScheme.surface else accentColor.copy(alpha = 0.08f),
        tonalElevation = if (alert.isRead) 1.dp else 3.dp,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isUrgent) Icons.Default.Warning else Icons.Default.Info,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(color = accentColor, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        alert.urgency,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                if (!alert.isRead) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(accentColor, shape = CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(alert.message, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                formatter.format(Date(alert.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}