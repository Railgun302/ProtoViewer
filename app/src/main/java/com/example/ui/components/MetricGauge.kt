package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.GridLine
import com.example.ui.theme.LaserCrimson
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun MetricGauge(
    value: Float,
    maxValue: Float = 100f,
    label: String,
    unit: String = "%",
    sizeDp: Dp = 92.dp,
    primaryColor: Color = CyberEmerald,
    modifier: Modifier = Modifier
) {
    val fraction = (value / maxValue).coerceIn(0f, 1f)
    val sweepColor = when {
        fraction > 0.85f -> LaserCrimson
        fraction > 0.70f -> SolarAmber
        else -> primaryColor
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(sizeDp)
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val strokeWidth = 7.dp.toPx()
            val arcSize = size.width - strokeWidth
            val arcOffset = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Background Track Arc (240 degrees sweep from 150 deg to 390 deg)
            drawArc(
                color = GridLine,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = arcOffset,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Sweep Arc
            val activeSweep = 240f * fraction
            if (activeSweep > 0f) {
                drawArc(
                    color = sweepColor,
                    startAngle = 150f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = arcOffset,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${value.toInt()}$unit",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = TextMuted
            )
        }
    }
}
