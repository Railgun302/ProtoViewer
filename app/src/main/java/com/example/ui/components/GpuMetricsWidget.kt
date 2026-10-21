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
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GpuMetrics
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GridLine
import com.example.ui.theme.LaserCrimson
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltYellow

@Composable
fun GpuMetricsWidget(
    gpu: GpuMetrics,
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
        gpu.tempCoreCelsius >= 80f -> LaserCrimson
        gpu.tempCoreCelsius >= 70f -> SolarAmber
        else -> ElectricCyan
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkStudioSurface, RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("gpu_metrics_card")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = "GPU",
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GPU DIAGNOSTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = gpu.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Gauge + Key Telemetry
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricGauge(
                value = gpu.usagePercent,
                maxValue = 100f,
                label = "LOAD",
                primaryColor = ElectricCyan,
                sizeDp = 58.dp
            )

            // Right side stats: Core Temp, Hotspot, Power (W), Fan RPM
            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TEMP / HOTSPOT", fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = "${formatTemp(gpu.tempCoreCelsius)} / ${formatTemp(gpu.tempHotspotCelsius)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = tempColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("POWER DRAW", fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = "${gpu.powerWatts.toInt()} W",
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
                        Text("VRAM ALLOCATED", fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = "${(gpu.vramUsedMb / 1024f).toInt()} GB / ${(gpu.vramTotalMb / 1024f).toInt()} GB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("FAN SPEED", fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = if (gpu.fanRpm > 0) "${gpu.fanRpm} RPM" else "0 RPM (Passive)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // VRAM Bar Progress
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "VRAM Usage",
                    fontSize = 9.sp,
                    color = TextMuted
                )
                Text(
                    text = "${gpu.vramPercent.toInt()}%",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = ElectricCyan
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(GridLine)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(gpu.vramPercent / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(ElectricCyan)
                )
            }
        }
    }
}
