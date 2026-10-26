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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strathmore.CareConnect.data.local.entities.Symptom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A line chart of recent symptom severity (1-10), with the exact number and log date
 * drawn directly at each point, so the trend is both visually readable (the line's
 * shape) and unambiguous (the actual numbers are printed on it).
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

            if (entries.size < 2) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Log at least 2 symptoms to see a trend here.",
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
                Spacer(modifier = Modifier.height(20.dp))

                val lineColor = MaterialTheme.colorScheme.secondary
                val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                val lowColor = MaterialTheme.colorScheme.primary
                val midColor = MaterialTheme.colorScheme.secondary
                val highColor = MaterialTheme.colorScheme.error
                val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    val leftPad = 8f
                    val rightPad = 8f
                    val topPad = 36f   // room for the number printed above each point
                    val bottomPad = 28f // room for the date printed below the axis
                    val chartWidth = size.width - leftPad - rightPad
                    val chartHeight = size.height - topPad - bottomPad

                    listOf(1, 5, 10).forEach { gridValue ->
                        val y = topPad + chartHeight - (gridValue / 10f) * chartHeight
                        drawLine(
                            color = gridColor,
                            start = Offset(leftPad, y),
                            end = Offset(size.width - rightPad, y),
                            strokeWidth = 1.5f
                        )
                    }

                    val stepX = chartWidth / (entries.size - 1)
                    val points = entries.mapIndexed { index, symptom ->
                        val x = leftPad + index * stepX
                        val y = topPad + chartHeight - (symptom.rating / 10f) * chartHeight
                        Offset(x, y)
                    }

                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = lineColor,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                    }

                    entries.forEachIndexed { index, symptom ->
                        val color = when {
                            symptom.rating >= 8 -> highColor
                            symptom.rating >= 5 -> midColor
                            else -> lowColor
                        }
                        val point = points[index]

                        drawCircle(color = color, radius = 8f, center = point)
                        drawCircle(color = Color.White, radius = 3.5f, center = point)

                        // Severity number, printed just above the point
                        drawContext.canvas.nativeCanvas.apply {
                            val numberPaint = android.graphics.Paint().apply {
                                this.color = color.toArgb()
                                textSize = 28f
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            drawText("${symptom.rating}", point.x, point.y - 16f, numberPaint)

                            // Date, printed at the bottom of the chart under this point
                            val datePaint = android.graphics.Paint().apply {
                                this.color = labelColor
                                textSize = 20f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            drawText(
                                dateFormatter.format(Date(symptom.date)),
                                point.x,
                                size.height - 6f,
                                datePaint
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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