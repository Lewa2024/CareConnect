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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo
import com.strathmore.CareConnect.ui.theme.Navy
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
    val latestEntry by viewModel.latestEntry.collectAsState()
    val latestAlert by viewModel.latestAlert.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(caregiverId) {
        viewModel.loadPatient(caregiverId)
    }

    fun showComingSoon(feature: String) {
        scope.launch { snackbarHostState.showSnackbar("$feature coming soon") }
    }

    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when {
        hour < 12 -> "Good morning,"
        hour < 17 -> "Good afternoon,"
        else -> "Good evening,"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PillNavBar(
                onHistory = { patient?.let { onViewHistory(it.patientId) } },
                onBook = { showComingSoon("Nurse booking") },
                onAlerts = { showComingSoon("Alerts") },
                onProfile = onProfile
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // --- Top row: logo + notification bell ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AppLogo()
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showComingSoon("Alerts") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Alerts",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Greeting ---
            Text(
                greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))

            if (patient != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewPatientDetails(patient!!.patientId) }
                ) {
                    Text(
                        "Caring for ${patient!!.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    IconButton(
                        onClick = { onEditPatient(patient!!.patientId) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit patient",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Text(
                    "Welcome to CareConnect",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Navy action card ---
            if (patient == null) {
                ActionHeroCard(
                    title = "Add a patient",
                    subtitle = "Set up a profile to start tracking",
                    onClick = onAddPatient
                )
            } else {
                ActionHeroCard(
                    title = "Log symptoms",
                    subtitle = "Record how ${patient!!.name} is feeling today",
                    onClick = { onLogSymptoms(patient!!.patientId) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Health overview (latest vitals) ---
            HealthOverviewCard(
                weightKg = latestEntry?.weightKg,
                systolicBp = latestEntry?.systolicBp,
                diastolicBp = latestEntry?.diastolicBp,
                medicationTaken = latestEntry?.medicationTaken
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Quick access ---
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Quick Access",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickAccessItem(
                            icon = Icons.Default.LocalHospital,
                            label = "Find Nurse",
                            highlighted = true,
                            modifier = Modifier.weight(1f)
                        ) { showComingSoon("Nurse booking") }
                        QuickAccessItem(
                            icon = Icons.Default.History,
                            label = "History",
                            modifier = Modifier.weight(1f)
                        ) { patient?.let { onViewHistory(it.patientId) } }
                        QuickAccessItem(
                            icon = Icons.Default.Person,
                            label = "Patient Info",
                            modifier = Modifier.weight(1f)
                        ) { patient?.let { onViewPatientDetails(it.patientId) } }
                        QuickAccessItem(
                            icon = Icons.Default.Notifications,
                            label = "Alerts",
                            modifier = Modifier.weight(1f)
                        ) { showComingSoon("Alerts") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CancerFactCard()

            Spacer(modifier = Modifier.height(12.dp))

            CaregiverTipCard()

            Spacer(modifier = Modifier.height(20.dp))

             Text("Recent alerts", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                val alert = latestAlert
                if (alert == null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActionHeroCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Navy, Color(0xFF223D6B))))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Daily check-in",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Open",
                    tint = Navy
                )
            }
        }
    }
}

@Composable
private fun HealthOverviewCard(
    weightKg: Double?,
    systolicBp: Int?,
    diastolicBp: Int?,
    medicationTaken: Boolean?
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Health Overview",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "Latest entry",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VitalTile(
                    label = "Weight",
                    value = weightKg?.let { "$it" } ?: "—",
                    unit = "kg",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                VitalTile(
                    label = "Blood Pressure",
                    value = if (systolicBp != null && diastolicBp != null) "$systolicBp/$diastolicBp" else "—",
                    unit = "mmHg",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                VitalTile(
                    label = "Medication",
                    value = when (medicationTaken) {
                        true -> "Taken"
                        false -> "Missed"
                        null -> "—"
                    },
                    unit = "today",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun VitalTile(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickAccessItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (highlighted) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun PillNavBar(
    onHistory: () -> Unit,
    onBook: () -> Unit,
    onAlerts: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selected tab: navy pill with label
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Navy)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Home", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            NavIcon(Icons.Default.History, "History", onHistory)
            NavIcon(Icons.Default.LocalHospital, "Book", onBook)
            NavIcon(Icons.Default.Notifications, "Alerts", onAlerts)
            NavIcon(Icons.Default.Person, "Profile", onProfile)
        }
    }
}

@Composable
private fun NavIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}