package com.example.ui

import android.app.Activity
import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connection.ConnectionMode
import com.example.connection.ConnectionStatus
import com.example.model.StudioWallpaper
import com.example.ui.components.CpuMetricsWidget
import com.example.ui.components.CustomizationSheet
import com.example.ui.components.GamingHudWidget
import com.example.ui.components.GpuMetricsWidget
import com.example.ui.components.LinuxSetupSheet
import com.example.ui.components.NvmeStorageWidget
import com.example.ui.components.PowerMatrixWidget
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkStudioBg
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
fun TelemetryDashboardScreen(
    viewModel: TelemetryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current

    // Keep screen awake if enabled
    DisposableEffect(uiState.config.keepScreenAwake) {
        val window = (context as? Activity)?.window
        if (uiState.config.keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                when (uiState.config.wallpaper) {
                    StudioWallpaper.AMOLED_BLACK -> AmoledBlack
                    StudioWallpaper.STUDIO_CARBON -> Color(0xFF06090E)
                    StudioWallpaper.CYBER_HEX -> Color(0xFF080D15)
                    StudioWallpaper.SLATE_DEEP -> DarkStudioBg
                }
            )
            .offset(x = uiState.pixelShiftX.dp, y = uiState.pixelShiftY.dp)
            .testTag("telemetry_dashboard_screen")
    ) {
        // Decorative background wallpaper canvas (if texture selected)
        if (uiState.config.wallpaper != StudioWallpaper.AMOLED_BLACK) {
            WallpaperCanvas(wallpaper = uiState.config.wallpaper)
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                DashboardHeader(
                    status = uiState.connectionStatus,
                    hz = uiState.config.refreshRate.hz,
                    onOpenSetup = { viewModel.showSetupSheet(true) },
                    onOpenConfig = { viewModel.showConfigSheet(true) }
                )
            }
        ) { paddingValues ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp)
            ) {
                if (isLandscape) {
                    // Landscape 2-Column Desktop Companion Layout (Ideal for Moto Z below monitor)
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Left Column: Gaming HUD + Power Matrix
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("left_dashboard_column"),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (uiState.config.showGamingHud) {
                                item {
                                    GamingHudWidget(
                                        gaming = uiState.snapshot.gaming,
                                        frametimes = uiState.frametimes
                                    )
                                }
                            }
                            if (uiState.config.showPowerCard) {
                                item {
                                    PowerMatrixWidget(
                                        power = uiState.snapshot.power,
                                        powerHistory = uiState.powerHistory
                                    )
                                }
                            }
                            if (uiState.config.showNvmeCard) {
                                item {
                                    NvmeStorageWidget(
                                        nvme = uiState.snapshot.nvme,
                                        net = uiState.snapshot.net,
                                        useFahrenheit = uiState.config.useFahrenheit
                                    )
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }

                        // Right Column: GPU Diagnostics + CPU Diagnostics
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("right_dashboard_column"),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (uiState.config.showGpuCard) {
                                item {
                                    GpuMetricsWidget(
                                        gpu = uiState.snapshot.gpu,
                                        useFahrenheit = uiState.config.useFahrenheit
                                    )
                                }
                            }
                            if (uiState.config.showCpuCard) {
                                item {
                                    CpuMetricsWidget(
                                        cpu = uiState.snapshot.cpu,
                                        ram = uiState.snapshot.ram,
                                        useFahrenheit = uiState.config.useFahrenheit
                                    )
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                } else {
                    // Portrait 1-Column Layout
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                            .testTag("portrait_dashboard_column"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.config.showGamingHud) {
                            item {
                                GamingHudWidget(
                                    gaming = uiState.snapshot.gaming,
                                    frametimes = uiState.frametimes
                                )
                            }
                        }
                        if (uiState.config.showPowerCard) {
                            item {
                                PowerMatrixWidget(
                                    power = uiState.snapshot.power,
                                    powerHistory = uiState.powerHistory
                                )
                            }
                        }
                        if (uiState.config.showGpuCard) {
                            item {
                                GpuMetricsWidget(
                                    gpu = uiState.snapshot.gpu,
                                    useFahrenheit = uiState.config.useFahrenheit
                                )
                            }
                        }
                        if (uiState.config.showCpuCard) {
                            item {
                                CpuMetricsWidget(
                                    cpu = uiState.snapshot.cpu,
                                    ram = uiState.snapshot.ram,
                                    useFahrenheit = uiState.config.useFahrenheit
                                )
                            }
                        }
                        if (uiState.config.showNvmeCard) {
                            item {
                                NvmeStorageWidget(
                                    nvme = uiState.snapshot.nvme,
                                    net = uiState.snapshot.net,
                                    useFahrenheit = uiState.config.useFahrenheit
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }

        // Linux Setup Modal
        if (uiState.isSetupSheetVisible) {
            LinuxSetupSheet(
                status = uiState.connectionStatus,
                onDismiss = { viewModel.showSetupSheet(false) },
                onSelectMode = { mode ->
                    viewModel.setConnectionMode(mode)
                    viewModel.showSetupSheet(false)
                }
            )
        }

        // Customization Modal
        if (uiState.isConfigSheetVisible) {
            CustomizationSheet(
                config = uiState.config,
                onDismiss = { viewModel.showConfigSheet(false) },
                onSelectRate = { viewModel.setRefreshRate(it) },
                onSelectWallpaper = { viewModel.setWallpaper(it) },
                onToggleWidget = { viewModel.toggleWidget(it) },
                onToggleUnit = { viewModel.toggleUnit() },
                onToggleShift = { viewModel.toggleAmoledShift() },
                onToggleScreenAwake = { viewModel.toggleScreenAwake() }
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    status: ConnectionStatus,
    hz: Int,
    onOpenSetup: () -> Unit,
    onOpenConfig: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title + Glowing Pulse Dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onOpenSetup() }
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        when (status) {
                            is ConnectionStatus.Connected -> CyberEmerald
                            is ConnectionStatus.Connecting -> SolarAmber
                            else -> TextMuted
                        },
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "OMNIDECK",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ARCH / CACHYOS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        modifier = Modifier
                            .background(Color(0xFF0F1B2B), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = when (status) {
                        is ConnectionStatus.Connected -> status.detail
                        is ConnectionStatus.Connecting -> status.message
                        is ConnectionStatus.Error -> status.message
                        ConnectionStatus.Disconnected -> "Disconnected"
                    },
                    fontSize = 9.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }

        // Action Buttons: Hz badge, Setup icon, Settings icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Sampling rate badge
            Box(
                modifier = Modifier
                    .background(DarkStudioSurface, RoundedCornerShape(6.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                    .clickable { onOpenConfig() }
                    .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${hz}Hz",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = VoltYellow
                )
            }

            // Linux Setup Sheet button
            IconButton(
                onClick = onOpenSetup,
                modifier = Modifier.testTag("linux_setup_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Linux Host Setup",
                    tint = CyberEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Customize & Tuning button
            IconButton(
                onClick = onOpenConfig,
                modifier = Modifier.testTag("customize_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Customize Dashboard",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun WallpaperCanvas(wallpaper: StudioWallpaper) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        when (wallpaper) {
            StudioWallpaper.STUDIO_CARBON -> {
                // Subtle diagonal carbon weave lines
                val step = 16.dp.toPx()
                for (x in 0..(size.width + size.height).toInt() step step.toInt()) {
                    drawLine(
                        color = GridLine.copy(alpha = 0.4f),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(0f, x.toFloat()),
                        strokeWidth = 1f
                    )
                }
            }
            StudioWallpaper.CYBER_HEX -> {
                // Horizontal grid lines
                val step = 28.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = GridLine.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)
                    )
                    y += step
                }
            }
            else -> {}
        }
    }
}
