package com.strathmore.CareConnect.ui.symptom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strathmore.CareConnect.data.local.entities.Symptom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A labeled bar chart of recent symptom severity (1-10): each bar shows its exact
 * number and the date it was logged, so the caregiver doesn't have to interpret an
 * abstract line — the values are printed right on the chart.
 */
@Composable
fun SymptomTrendChart(recentEntries: List<Symptom>, modifier: Modifier = Modifier) {
    val entries = remember(recentEntries) { recentEntries.reversed() } // oldest to newest, left to right
    val dateFormatter = remember { SimpleDateFormat("d/M", Locale.US) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Symptom Trend", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Log a symptom to see your trend here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    "Severity (out of 10) for your last ${entries.size} entries",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                val lowColor = MaterialTheme.colorScheme.primary
                val midColor = MaterialTheme.colorScheme.secondary
                val highColor = MaterialTheme.colorScheme.error
                val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

                Row(
                    modifier = Modifier.fillMaxWidth().height(170.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    entries.forEach { symptom ->
                        val color = when {
                            symptom.rating >= 8 -> highColor
                            symptom.rating >= 5 -> midColor
                            else -> lowColor
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            Text(
                                "${symptom.rating}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Canvas(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(0.5f)
                            ) {
                                val barHeight = size.height * (symptom.rating / 10f)
                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(0f, size.height - barHeight),
                                    size = Size(size.width, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                dateFormatter.format(Date(symptom.date)),
                                style = MaterialTheme.typography.labelSmall,
                                color = labelColor,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LegendDot(color = lowColor, label = "Mild (1\u20134)")
                    LegendDot(color = midColor, label = "Moderate (5\u20137)")
                    LegendDot(color = highColor, label = "Severe (8\u201310)")
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}