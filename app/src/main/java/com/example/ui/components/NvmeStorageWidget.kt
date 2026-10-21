package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
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
import com.example.model.NetworkMetrics
import com.example.model.NvmeMetrics
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LaserCrimson
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NvmeStorageWidget(
    nvme: NvmeMetrics,
    net: NetworkMetrics,
    useFahrenheit: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tempDisplay = if (useFahrenheit) {
        "${(nvme.tempCelsius * 9f / 5f + 32f).toInt()}°F"
    } else {
        "${nvme.tempCelsius.toInt()}°C"
    }

    val (badgeBg, badgeText, badgeColor) = when {
        nvme.tempCelsius >= 70f -> Triple(Color(0x33FF3366), "THERMAL ALERT", LaserCrimson)
        nvme.tempCelsius >= 60f -> Triple(Color(0x33FFB703), "WARM", SolarAmber)
        else -> Triple(Color(0x2200E5A3), "OPTIMAL", CyberEmerald)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkStudioSurface, RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("nvme_storage_card")
    ) {
        // NVMe Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "NVMe Storage",
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NVMe & NETWORK I/O",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = nvme.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // NVMe Thermals & Throughput
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // NVMe Temperature
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("NVMe TEMP", fontSize = 9.sp, color = TextMuted)
                    Text(
                        text = tempDisplay,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = badgeColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (nvme.tempCelsius >= 70f) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = LaserCrimson,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }

            // Disk Read / Write
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("READ SPEED", fontSize = 9.sp, color = TextMuted)
                    Text(
                        text = "${nvme.readMbps.toInt()} MB/s",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyberEmerald
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("WRITE SPEED", fontSize = 9.sp, color = TextMuted)
                    Text(
                        text = "${nvme.writeMbps.toInt()} MB/s",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ElectricCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Network Bandwidth Sub-row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF090D14), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Network",
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ETHERNET / WI-FI",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Download",
                        tint = CyberEmerald,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${net.downMbps} Mbps",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Upload",
                        tint = ElectricCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${net.upMbps} Mbps",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
