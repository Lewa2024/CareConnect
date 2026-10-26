package com.strathmore.CareConnect.ui.symptom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPainGuideSheet(
    onDismiss: () -> Unit,
    onViewFullGuide: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Quick Pain Guide",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Not sure what number to pick? Use this as a rough guide.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            QuickBand(
                range = "1\u20133",
                label = "Mild",
                color = MaterialTheme.colorScheme.primary,
                description = "Noticeable, but doesn't stop daily activities."
            )
            QuickBand(
                range = "4\u20136",
                label = "Moderate",
                color = MaterialTheme.colorScheme.secondary,
                description = "Gets in the way of normal activities."
            )
            QuickBand(
                range = "7\u201310",
                label = "Severe",
                color = MaterialTheme.colorScheme.error,
                description = "Disabling \u2014 hard to focus on anything else."
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onViewFullGuide,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View full guide with tips \u2192")
            }
        }
    }
}

@Composable
private fun QuickBand(range: String, label: String, color: androidx.compose.ui.graphics.Color, description: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = color, shape = RoundedCornerShape(20.dp)) {
                Text(
                    range,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}