package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DisplayConfig
import com.example.model.RefreshRate
import com.example.model.StudioWallpaper
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationSheet(
    config: DisplayConfig,
    onDismiss: () -> Unit,
    onSelectRate: (RefreshRate) -> Unit,
    onSelectWallpaper: (StudioWallpaper) -> Unit,
    onToggleWidget: (String) -> Unit,
    onToggleUnit: () -> Unit,
    onToggleShift: () -> Unit,
    onToggleScreenAwake: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AmoledBlack,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("customization_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = CyberEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STUDIO & DISPLAY TUNING",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Refresh Rates
            Text(
                text = "REFRESH RATE & SAMPLING FREQUENCY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RefreshRate.entries.forEach { rate ->
                    val isSelected = config.refreshRate == rate
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) CyberEmerald.copy(alpha = 0.15f) else DarkStudioSurface,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) CyberEmerald else CardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectRate(rate) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${rate.hz} Hz",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CyberEmerald else TextPrimary
                            )
                            Text(
                                text = "${rate.intervalMs}ms",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Studio Wallpaper
            Text(
                text = "AMOLED WALLPAPER & THEME",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StudioWallpaper.entries.forEach { wp ->
                    val isSelected = config.wallpaper == wp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) ElectricCyan.copy(alpha = 0.12f) else DarkStudioSurface,
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) ElectricCyan else CardBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectWallpaper(wp) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = wp.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) ElectricCyan else TextPrimary
                            )
                            Text(
                                text = wp.description,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(ElectricCyan, RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Toggleable Widgets
            Text(
                text = "ACTIVE WIDGET TILES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingToggleRow(
                title = "Gaming HUD & MangoHud Frametimes",
                enabled = config.showGamingHud,
                onToggle = { onToggleWidget("gaming") }
            )
            SettingToggleRow(
                title = "GPU Diagnostics & VRAM Meter",
                enabled = config.showGpuCard,
                onToggle = { onToggleWidget("gpu") }
            )
            SettingToggleRow(
                title = "CPU Metrics & Package Thermals",
                enabled = config.showCpuCard,
                onToggle = { onToggleWidget("cpu") }
            )
            SettingToggleRow(
                title = "System Power Matrix (Watts Draw)",
                enabled = config.showPowerCard,
                onToggle = { onToggleWidget("power") }
            )
            SettingToggleRow(
                title = "NVMe Thermals & Drive I/O",
                enabled = config.showNvmeCard,
                onToggle = { onToggleWidget("nvme") }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Hardware & Display Safety
            Text(
                text = "HARDWARE & MOTO Z AMOLED SETTINGS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingToggleRow(
                title = "AMOLED Burn-In Pixel Shift Protection",
                subtitle = "Subtly shifts UI subpixels every few minutes",
                enabled = config.amoledShiftEnabled,
                onToggle = onToggleShift
            )
            SettingToggleRow(
                title = "Keep Screen Awake (Companion Mode)",
                subtitle = "Prevents device from sleeping while displaying telemetry",
                enabled = config.keepScreenAwake,
                onToggle = onToggleScreenAwake
            )
            SettingToggleRow(
                title = "Temperature Unit in Fahrenheit (°F)",
                subtitle = if (config.useFahrenheit) "Displaying in °F" else "Displaying in Celsius (°C)",
                enabled = config.useFahrenheit,
                onToggle = onToggleUnit
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String? = null,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = AmoledBlack,
                checkedTrackColor = CyberEmerald,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkStudioSurface
            )
        )
    }
}
