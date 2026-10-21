package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GamingMetrics
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GamingHudWidget(
    gaming: GamingMetrics,
    frametimes: List<Float>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkStudioSurface, RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("gaming_hud_card")
    ) {
        // Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "Gaming HUD",
                    tint = CyberEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GAMING HUD (MANGOHUD)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
            }
            if (gaming.gameTitle.isNotEmpty()) {
                Text(
                    text = gaming.gameTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = ElectricCyan,
                    modifier = Modifier
                        .background(Color(0xFF131D2D), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Primary Readouts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (gaming.fps > 0f) String.format("%.1f", gaming.fps) else "--.-",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = CyberEmerald
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FPS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // 1% Low and Frametime
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "1% LOW",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Text(
                        text = if (gaming.fps1PercentLow > 0f) "${gaming.fps1PercentLow.toInt()}" else "--",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = SolarAmber
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "FRAMETIME",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Text(
                        text = if (gaming.frametimeMs > 0f) String.format("%.2f ms", gaming.frametimeMs) else "--",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Real-time Canvas Graph
        FrametimeGraph(frametimes = frametimes, modifier = Modifier.weight(1f, fill = false))
    }
}
