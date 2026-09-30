package com.strathmore.CareConnect.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.strathmore.CareConnect.ui.components.AppLogo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    caregiverId: Long,
    onLogSymptoms: (patientId: Long) -> Unit,
    onViewHistory: (patientId: Long) -> Unit,
    onAddPatient: () -> Unit,
    onEditPatient: (patientId: Long) -> Unit,
    onViewPatientDetails: (patientId: Long) -> Unit,
    onProfile: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val patient by viewModel.patient.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(caregiverId) {
        viewModel.loadPatient(caregiverId)
    }

    fun showComingSoon(feature: String) {
        scope.launch { snackbarHostState.showSnackbar("$feature coming soon") }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { AppLogo() })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* already home */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { patient?.let { onViewHistory(it.patientId) } },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { showComingSoon("Nurse booking") },
                    icon = { Icon(Icons.Default.LocalHospital, contentDescription = "Book") },
                    label = { Text("Book") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { showComingSoon("Alerts") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                "Good day",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (patient != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewPatientDetails(patient!!.patientId) }
                ) {
                    Text(
                        "Caring for: ${patient!!.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = { onEditPatient(patient!!.patientId) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit patient",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Text(
                    "No patient added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (patient == null) {
                Button(
                    onClick = onAddPatient,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("+ ADD PATIENT")
                }
            } else {
                Button(
                    onClick = { onLogSymptoms(patient!!.patientId) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("+ LOG SYMPTOMS")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showComingSoon("Nurse booking") },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("FIND A NURSE")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text("Recent alerts", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No alerts yet — the rule engine isn't built yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}