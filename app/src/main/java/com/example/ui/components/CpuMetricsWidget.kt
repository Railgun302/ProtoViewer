package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CpuMetrics
import com.example.model.RamMetrics
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.GridLine
import com.example.ui.theme.LaserCrimson
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltYellow

@Composable
fun CpuMetricsWidget(
    cpu: CpuMetrics,
    ram: RamMetrics,
    useFahrenheit: Boolean = false,
    modifier: Modifier = Modifier
) {
    fun formatTemp(celsius: Float): String {
        return if (useFahrenheit) {
            "${(celsius * 9f / 5f + 32f).toInt()}°F"
        } else {
            "${celsius.toInt()}°C"
        }
    }

    val tempColor = when {
        cpu.tempCelsius >= 85f -> LaserCrimson
        cpu.tempCelsius >= 75f -> SolarAmber
        else -> CyberEmerald
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkStudioSurface, RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("cpu_metrics_card")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DeveloperBoard,
                    contentDescription = "CPU",
                    tint = CyberEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CPU & SYSTEM MEMORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = "${(cpu.freqMhz / 1000f)} GHz",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = CyberEmerald
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Gauge + Key Telemetry
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricGauge(
                value = cpu.usagePercent,
                maxValue = 100f,
                label = "CPU LOAD",
                primaryColor = CyberEmerald,
                sizeDp = 86.dp
            )

            // Right side stats: Package Temp, RAPL/Zenpower Wattage, RAM Usage
            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("PACKAGE TEMP", fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = formatTemp(cpu.tempCelsius),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = tempColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("CPU POWER (RAPL)", fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = "${cpu.powerWatts.toInt()} W",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = VoltYellow
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("SYSTEM RAM USED", fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = "${(ram.usedMb / 1024f).toInt()} GB / ${(ram.totalMb / 1024f).toInt()} GB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("AVG FREQUENCY", fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = "${cpu.freqMhz} MHz",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Multi-core Mini Bars
        if (cpu.coreUsages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                cpu.coreUsages.take(16).forEach { coreLoad ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(GridLine),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight((coreLoad / 100f).coerceIn(0.05f, 1f))
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (coreLoad > 85f) SolarAmber else CyberEmerald)
                        )
                    }
                }
            }
        }
    }
}
