package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PowerMetrics
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GridLine
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltYellow

@Composable
fun PowerMatrixWidget(
    power: PowerMetrics,
    powerHistory: List<Float>,
    modifier: Modifier = Modifier
) {
    val totalWatts = if (power.totalWatts > 0f) power.totalWatts else (power.cpuWatts + power.gpuWatts)
    val cpuFraction = if (totalWatts > 0f) (power.cpuWatts / totalWatts).coerceIn(0f, 1f) else 0.5f
    val gpuFraction = if (totalWatts > 0f) (power.gpuWatts / totalWatts).coerceIn(0f, 1f) else 0.5f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkStudioSurface, RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("power_matrix_card")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Power",
                    tint = VoltYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SYSTEM POWER MATRIX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = "PEAK: ${power.peakWatts.toInt()} W",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Large Total Power readout + Sparkline
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "REAL-TIME DRAW",
                    fontSize = 9.sp,
                    color = TextMuted
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${totalWatts.toInt()}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = VoltYellow
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WATTS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Power Sparkline
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(44.dp)
                    .background(Color(0xFF090D14), RoundedCornerShape(6.dp))
                    .padding(4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (powerHistory.size < 2) return@Canvas
                    val maxW = 500f
                    val step = size.width / (powerHistory.size - 1).coerceAtLeast(1)
                    val path = Path()

                    powerHistory.forEachIndexed { i, w ->
                        val x = i * step
                        val y = size.height - (w / maxW * size.height).coerceIn(0f, size.height)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = VoltYellow,
                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CPU vs GPU Wattage Ratio Bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "CPU: ${power.cpuWatts.toInt()} W",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CyberEmerald
                )
                Text(
                    text = "GPU: ${power.gpuWatts.toInt()} W",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = ElectricCyan
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(3.5.dp))
                    .background(GridLine)
            ) {
                Box(
                    modifier = Modifier
                        .weight(cpuFraction.coerceAtLeast(0.05f))
                        .fillMaxHeight()
                        .background(CyberEmerald)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .weight(gpuFraction.coerceAtLeast(0.05f))
                        .fillMaxHeight()
                        .background(ElectricCyan)
                )
            }
        }
    }
}
