package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.utils.DateUtils
import kotlin.math.max

@Composable
fun WeeklySpendTrendChart(
    weeklyExpenses: Map<Int, Double>, // DayIndex (1..7) -> amount
    recommendedDailyAllowance: Double,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxSpent = max(100.0, weeklyExpenses.values.maxOrNull() ?: 0.0)
    val maxChartVal = max(maxSpent, recommendedDailyAllowance * 1.2)
    
    // Theme references matching screenshots
    val primaryColor = Color(0xFF8F70F8) // Elegant Lavender / Electric purple (matching Screen 1 charts)
    val allowanceColor = Color(0xFFD2FD31) // Neon Volt-yellow for daily limit line
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val errorColor = Color(0xFFFF5252) // Neon Red/Pink for high spend

    var animationTriggered by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "chart_anim"
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "Weekly Activity Trend",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val itemHeight = 150.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(itemHeight)) {
                val chartWidth = size.width
                val chartHeight = size.height - 30.dp.toPx() // leave space for labels
                val colProgress = progress

                // Draw Y-Axis Guidelines (0%, 50%, 100%)
                for (i in 0..2) {
                    val y = chartHeight * (i / 2f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Recommended Daily Allowance line (dashboard benchmark)
                val allowanceY = (1f - (recommendedDailyAllowance / maxChartVal).toFloat()) * chartHeight
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                drawLine(
                    color = allowanceColor,
                    start = Offset(0f, allowanceY),
                    end = Offset(chartWidth, allowanceY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = pathEffect
                )

                // Draw Bars for each day of the week
                val numDays = 7
                val spacingVal = 14.dp.toPx()
                val availableWidth = chartWidth - (spacingVal * (numDays + 1))
                val barWidth = availableWidth / numDays

                for (dayIndex in 1..7) {
                    val spent = weeklyExpenses[dayIndex] ?: 0.0
                    val ratio = (spent / maxChartVal).toFloat().coerceIn(0f, 1f)
                    val barHeight = ratio * chartHeight * colProgress

                    val x = spacingVal * dayIndex + barWidth * (dayIndex - 1)
                    val y = chartHeight - barHeight

                    // Draw the spending bar
                    drawRoundRect(
                        color = if (spent > recommendedDailyAllowance) errorColor else primaryColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            // Labels Row beneath the canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 130.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Generate simple aligned labels
                dayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Legend details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Spent", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = labelColor)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(errorColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = labelColor)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.width(18.dp).height(2.dp)) {
                    drawLine(
                        color = allowanceColor,
                        start = Offset.Zero,
                        end = Offset(size.width, 0f),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Limit ($currencySymbol${String.format("%.0f", recommendedDailyAllowance)})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelColor
                )
            }
        }
    }
}

@Composable
fun CategoryBreakdownChart(
    categoryTotals: Map<String, Double>,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val totalSpent = categoryTotals.values.sum()
    val isDataEmpty = totalSpent <= 0.0

    val categories = categoryTotals.keys.toList()

    // Distinctive high contrast modern neon colors for Neo-brutalist dark aesthetic
    val chartColors = listOf(
        Color(0xFFD2FD31), // Volt Lime-Yellow
        Color(0xFF8F70F8), // Lavender Purple
        Color(0xFF00E5FF), // Electric Cyan
        Color(0xFFFF2E93), // Hot Pink
        Color(0xFFFF9100), // Intense Orange
        Color(0xFF00E676), // Acid Green
        Color(0xFFB0BEC5)  // Soft Gray-Blue
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "Category Expenses",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isDataEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No spending logs registered yet.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Donut Canvas
                Box(
                    modifier = Modifier
                        .size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(110.dp)) {
                        var startAngle = -90f
                        categoryTotals.forEach { (cat, spent) ->
                            val arcAngle = ((spent / totalSpent) * 360f).toFloat()
                            val categoryColor = chartColors[max(0, categories.indexOf(cat)) % chartColors.size]
                            
                            drawArc(
                                color = categoryColor,
                                startAngle = startAngle,
                                sweepAngle = arcAngle,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                            startAngle += arcAngle
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Spent",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currencySymbol${String.format("%,.0f", totalSpent)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right: Categories list
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryTotals.entries.sortedByDescending { it.value }.take(5).forEach { (category, spent) ->
                        val colorIndex = max(0, categories.indexOf(category)) % chartColors.size
                        val catColor = chartColors[colorIndex]
                        val pct = (spent / totalSpent) * 100

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(0.55f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "$currencySymbol${String.format("%.0f", spent)} (${String.format("%.0f", pct)}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(0.45f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}
