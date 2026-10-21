package com.strathmore.CareConnect.ui.symptom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.strathmore.CareConnect.ui.components.AppLogo

private data class PainBand(
    val range: String,
    val label: String,
    val color: androidx.compose.ui.graphics.Color,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PainGuideScreen(onBack: () -> Unit) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "How to Rate Pain",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "A quick guide to help you choose a number when logging a symptom. " +
                    "The scale goes from 0 (no pain) to 10 (worst pain imaginable). " +
                    "If you're not sure, think about how much the pain is getting in the way of what the patient normally does.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val bands = listOf(
                PainBand(
                    range = "1–3",
                    label = "Mild",
                    color = MaterialTheme.colorScheme.primary,
                    description = "Noticeable but not disruptive — often described as nagging, annoying, " +
                        "or distracting. Usually doesn't stop the patient from doing their normal daily activities."
                ),
                PainBand(
                    range = "4–6",
                    label = "Moderate",
                    color = MaterialTheme.colorScheme.secondary,
                    description = "Gets in the way of normal daily activities. The patient might still be able " +
                        "to push through or ignore it briefly if deeply focused on something else, but it's a clear interruption."
                ),
                PainBand(
                    range = "7–10",
                    label = "Severe",
                    color = MaterialTheme.colorScheme.error,
                    description = "Disabling — hard to focus on anything else. Often described as intense, " +
                        "unbearable, or excruciating, and may make it difficult to sleep or rest."
                )
            )

            bands.forEach { band ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = band.color.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Surface(
                                color = band.color,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    band.range,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(band.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(band.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tips for choosing a number", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Ask the patient directly if they're able to answer.\n" +
                            "• Watch for signs like grimacing, guarding a body part, reluctance to move, or changes in breathing.\n" +
                            "• Compare it to how they're normally doing — a sudden change matters even within the same band.\n" +
                            "• There's no wrong answer — consistency over time matters more than precision on any single day.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text(
                "Based on cancer-specific pain severity research (Serlin et al., 1995) and patient education guidance from Memorial Sloan Kettering Cancer Center.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}