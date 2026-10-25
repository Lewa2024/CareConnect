package com.strathmore.CareConnect.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletedSymptomsScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val deletionLog by viewModel.deletionLog.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDeletionLog()
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
            Text(
                "Deletion Audit Log",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            Text(
                "A record that entries were deleted, and when \u2014 for traceability only. " +
                    "Patient names and symptom details are not shown here to protect patient privacy; " +
                    "the underlying data is still kept in the database.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "${deletionLog.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "total entries deleted system-wide",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (deletionLog.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No deletions recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val formatter = remember { SimpleDateFormat("EEE, d MMM yyyy \u00B7 h:mm a", Locale.US) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(deletionLog) { entry ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Entry deleted \u2014 ${formatter.format(Date(entry.deletedAt))}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}