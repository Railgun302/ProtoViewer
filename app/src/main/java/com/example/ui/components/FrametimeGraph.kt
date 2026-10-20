package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GridLine
import com.example.ui.theme.LaserCrimson
import com.example.ui.theme.TextMuted

@Composable
fun FrametimeGraph(
    frametimes: List<Float>,
    modifier: Modifier = Modifier,
    maxFrametimeMs: Float = 25f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFF0A0E17), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width <= 0f || height <= 0f) return@Canvas

            // Target reference lines: 16.6ms (60 FPS) and 6.94ms (144 FPS)
            val y60Fps = height - (16.6f / maxFrametimeMs * height).coerceIn(0f, height)
            val y144Fps = height - (6.94f / maxFrametimeMs * height).coerceIn(0f, height)

            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

            // 16.6ms Line (60 FPS)
            drawLine(
                color = GridLine.copy(alpha = 0.8f),
                start = Offset(0f, y60Fps),
                end = Offset(width, y60Fps),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )

            // 6.94ms Line (144 FPS)
            drawLine(
                color = ElectricCyan.copy(alpha = 0.4f),
                start = Offset(0f, y144Fps),
                end = Offset(width, y144Fps),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )

            if (frametimes.size < 2) return@Canvas

            val stepX = width / (frametimes.size - 1).coerceAtLeast(1)
            val strokePath = Path()
            val fillPath = Path()

            val points = frametimes.mapIndexed { i, ms ->
                val x = i * stepX
                val clampedMs = ms.coerceIn(0f, maxFrametimeMs)
                val y = height - (clampedMs / maxFrametimeMs * height)
                Offset(x, y)
            }

            strokePath.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, height)
            fillPath.lineTo(points[0].x, points[0].y)

            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val midX = (prev.x + curr.x) / 2f
                strokePath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                fillPath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Draw gradient fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyberEmerald.copy(alpha = 0.28f),
                        CyberEmerald.copy(alpha = 0.02f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw sparkline stroke
            val latestMs = frametimes.lastOrNull() ?: 0f
            val strokeColor = if (latestMs > 20f) LaserCrimson else CyberEmerald

            drawPath(
                path = strokePath,
                color = strokeColor,
                style = Stroke(width = 2.2f, cap = StrokeCap.Round)
            )

            // Glowing indicator at the latest point
            val lastPoint = points.last()
            drawCircle(
                color = strokeColor.copy(alpha = 0.4f),
                radius = 5.5f,
                center = lastPoint
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = lastPoint
            )
        }

        // Overlay reference labels
        Text(
            text = "6.9ms (144 FPS)",
            color = ElectricCyan.copy(alpha = 0.6f),
            fontSize = 8.sp,
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Text(
            text = "16.6ms (60 FPS)",
            color = TextMuted,
            fontSize = 8.sp,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}
