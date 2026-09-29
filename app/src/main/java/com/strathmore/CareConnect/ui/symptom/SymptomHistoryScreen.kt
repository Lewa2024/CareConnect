package com.strathmore.CareConnect.ui.symptom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.data.local.entities.Symptom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomHistoryScreen(
    patientId: Long,
    onBackToHome: () -> Unit,
    viewModel: SymptomHistoryViewModel = viewModel()
) {
    val history by viewModel.history.collectAsState()

    LaunchedEffect(patientId) {
        viewModel.loadHistory(patientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Symptom History") },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Home")
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No symptoms logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(history) { symptom ->
                    SymptomHistoryCard(symptom)
                }
            }
        }
    }
}

@Composable
private fun SymptomHistoryCard(symptom: Symptom) {
    val formatter = remember { SimpleDateFormat("EEE, d MMM yyyy · h:mm a", Locale.US) }
    val severityColor = when {
        symptom.rating >= 8 -> MaterialTheme.colorScheme.error
        symptom.rating >= 5 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(symptom.type, fontWeight = FontWeight.Bold)
                Surface(
                    color = severityColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Severity ${symptom.rating}/10",
                        color = severityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                formatter.format(Date(symptom.date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (symptom.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(symptom.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}