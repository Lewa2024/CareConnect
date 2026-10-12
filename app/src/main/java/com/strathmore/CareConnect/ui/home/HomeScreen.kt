package com.strathmore.CareConnect.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.R
import com.strathmore.CareConnect.ui.components.AppLogo
import com.strathmore.CareConnect.ui.theme.GreenDeep
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(caregiverId) {
        viewModel.loadPatient(caregiverId)
    }

    fun showComingSoon(feature: String) {
        scope.launch { snackbarHostState.showSnackbar("$feature coming soon") }
    }

    Scaffold(
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // --- Hero section: dark green header + floating vitals card ---
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreenDeep)
                        .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 44.dp)
                ) {
                    AppLogo(modifier = Modifier.padding(bottom = 16.dp))

                    Text(
                        "Good day",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            IconButton(
                                onClick = { onEditPatient(patient!!.patientId) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit patient",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            "No patient added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                if (latestEntry != null) {
                    VitalsSummaryCard(
                        weightKg = latestEntry!!.weightKg,
                        systolicBp = latestEntry!!.systolicBp,
                        diastolicBp = latestEntry!!.diastolicBp,
                        medicationTaken = latestEntry!!.medicationTaken,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 36.dp)
                            .padding(horizontal = 20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (latestEntry != null) 52.dp else 20.dp))

            // --- White content area below the hero ---
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                MemorialSlideshow(
                    slides = listOf(
                        MemorialSlide(painter = { painterResource(R.drawable.uncle_photo_1) })
                    ),
                    dedicationName = "Josiah Mwatelah"
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(20.dp))

                CancerFactCard()

                Spacer(modifier = Modifier.height(12.dp))

                CaregiverTipCard()

                Spacer(modifier = Modifier.height(20.dp))

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

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun VitalsSummaryCard(
    weightKg: Double?,
    systolicBp: Int?,
    diastolicBp: Int?,
    medicationTaken: Boolean?,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Latest vitals",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                VitalStat(label = "Weight", value = weightKg?.let { "$it kg" } ?: "—")
                VitalStat(
                    label = "Blood Pressure",
                    value = if (systolicBp != null && diastolicBp != null) "$systolicBp/$diastolicBp" else "—"
                )
                VitalStat(
                    label = "Medication",
                    value = when (medicationTaken) {
                        true -> "Taken"
                        false -> "Missed"
                        null -> "—"
                    }
                )
            }
        }
    }
}

@Composable
private fun VitalStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}