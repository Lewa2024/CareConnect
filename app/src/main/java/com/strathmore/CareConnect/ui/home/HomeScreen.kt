package com.strathmore.CareConnect.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.fgraphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo
import com.strathmore.CareConnect.ui.symptom.SymptomTrendChart
import com.strathmore.CareConnect.ui.theme.Ice
import com.strathmore.CareConnect.ui.theme.Navy
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    caregiverId: Long,
    onLogSymptoms: (patientId: Long) -> Unit,
    onViewHistory: (patientId: Long) -> Unit,
    onAddPatient: () -> Unit,
    onEditPatient: (patientId: Long) -> Unit,
    onViewPatientDetails: (patientId: Long) -> Unit,
    onViewAlerts: (patientId: Long) -> Unit,
    onPainGuide: () -> Unit,
    onProfile: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val patient by viewModel.patient.collectAsState()
    val caregiverFirstName by viewModel.caregiverFirstName.collectAsState()
    val latestEntry by viewModel.latestEntry.collectAsState()
    val latestAlert by viewModel.latestAlert.collectAsState()
    val trend by viewModel.trend.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(caregiverId) {
        viewModel.loadPatient(caregiverId)
    }

    fun showComingSoon(feature: String) {
        scope.launch { snackbarHostState.showSnackbar("$feature coming soon") }
    }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { AppLogo() },
                actions = {
                    IconButton(onClick = { patient?.let { onViewAlerts(it.patientId) } ?: showComingSoon("Add a patient first") }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                    }
                    IconButton(onClick = onProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
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
                    onClick = { patient?.let { onViewAlerts(it.patientId) } ?: showComingSoon("Add a patient first") },
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                caregiverFirstName?.let { "$greeting, $it" } ?: greeting,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Dark navy card: patient + quick log button, or Add Patient pill ---
            if (patient != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Navy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onViewPatientDetails(patient!!.patientId) }
                            ) {
                                Text(
                                    "Caring for",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    patient!!.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = { onEditPatient(patient!!.patientId) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit patient", tint = Color.White.copy(alpha = 0.85f))
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                onClick = { onLogSymptoms(patient!!.patientId) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, contentDescription = "Log symptom", tint = Navy, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        if (latestEntry != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Last logged: ${latestEntry!!.type}, ${latestEntry!!.rating}/10",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            } else {
                ActionPillButton(
                    title = "Add Patient",
                    subtitle = "Register who you're caring for",
                    onClick = onAddPatient
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Alerts: kept near the top so nothing important gets missed ---
            Text("Recent alerts", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            val alert = latestAlert
            if (alert == null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Ice,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No alerts yet. The rule engine checks every symptom you log.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                val isUrgent = alert.urgency == "URGENT"
                val accentColor = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = accentColor.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { patient?.let { onViewHistory(it.patientId) } }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(color = accentColor, shape = RoundedCornerShape(20.dp)) {
                            Text(
                                alert.urgency,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            alert.message,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Health Overview",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            VitalsSummaryCard(
                weightKg = latestEntry?.weightKg,
                systolicBp = latestEntry?.systolicBp,
                diastolicBp = latestEntry?.diastolicBp,
                medicationTaken = latestEntry?.medicationTaken
            )

            Spacer(modifier = Modifier.height(16.dp))

            SymptomTrendChart(recentEntries = trend)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Quick Access",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickAccessItem(
                    icon = Icons.Default.Edit,
                    label = "Log\nSymptom",
                    onClick = { patient?.let { onLogSymptoms(it.patientId) } ?: showComingSoon("Add a patient first") }
                )
                QuickAccessItem(
                    icon = Icons.Default.History,
                    label = "History",
                    onClick = { patient?.let { onViewHistory(it.patientId) } ?: showComingSoon("Add a patient first") }
                )
                QuickAccessItem(
                    icon = Icons.Default.Healing,
                    label = "Pain\nGuide",
                    onClick = onPainGuide
                )
                QuickAccessItem(
                    icon = Icons.Default.LocalHospital,
                    label = "Find a\nNurse",
                    onClick = { showComingSoon("Nurse booking") }
                )
            }

            if (patient != null) {
                Spacer(modifier = Modifier.height(24.dp))

                ActionPillButton(
                    title = "Find a Nurse",
                    subtitle = "Search nearby available nurses",
                    onClick = { showComingSoon("Nurse booking") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            CancerFactCard()

            Spacer(modifier = Modifier.height(12.dp))

            CaregiverTipCard()

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ActionPillButton(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Navy,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 10.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
            }
            Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickAccessItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).width(72.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Ice,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = Navy, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 14.sp)
    }
}

@Composable
private fun VitalsSummaryCard(
    weightKg: Double?,
    systolicBp: Int?,
    diastolicBp: Int?,
    medicationTaken: Boolean?
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Ice,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VitalStat(label = "Weight", value = weightKg?.let { "$it kg" } ?: "\u2014")
            VitalStat(
                label = "Blood Pressure",
                value = if (systolicBp != null && diastolicBp != null) "$systolicBp/$diastolicBp" else "\u2014"
            )
            VitalStat(
                label = "Medication",
                value = when (medicationTaken) {
                    true -> "Taken"
                    false -> "Missed"
                    null -> "\u2014"
                }
            )
        }
    }
}

@Composable
private fun VitalStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Navy)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}