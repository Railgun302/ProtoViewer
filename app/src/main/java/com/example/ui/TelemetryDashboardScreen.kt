package com.example.ui

import android.app.Activity
import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltYellow

@Composable
fun TelemetryDashboardScreen(
    viewModel: TelemetryViewModel,
    modifier: Modifier = Modifier,
    onToggleFullscreen: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    var isFullscreenActive by remember { mutableStateOf(true) }

    // Keep screen awake (essential for companion desktop display)
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
        if (uiState.config.wallpaper != StudioWallpaper.AMOLED_BLACK) {
            WallpaperCanvas(wallpaper = uiState.config.wallpaper)
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                DashboardHeader(
                    status = uiState.connectionStatus,
                    rateBadge = uiState.config.refreshRate.badge,
                    isLandscape = isLandscape,
                    isFullscreen = isFullscreenActive,
                    onToggleFullscreen = {
                        isFullscreenActive = !isFullscreenActive
                        onToggleFullscreen(isFullscreenActive)
                    },
                    onOpenSetup = { viewModel.showSetupSheet(true) },
                    onOpenConfig = { viewModel.showConfigSheet(true) }
                )
            }
        ) { paddingValues ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = if (isLandscape) 8.dp else 12.dp)
            ) {
                val availableWidth = maxWidth
                val availableHeight = maxHeight

                if (isLandscape) {
                    // Fully adaptive Landscape Layout that scales to pixel dimensions and supports bidirectional scrolling
                    LandscapeAdaptiveLayout(
                        uiState = uiState,
                        availableWidth = availableWidth,
                        availableHeight = availableHeight
                    )
                } else {
                    // Portrait 1-Column Layout
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp)
                            .testTag("portrait_dashboard_column"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        item { Spacer(modifier = Modifier.height(20.dp)) }
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

/**
 * Adaptive Landscape Layout:
 * Dynamically computes column width based on display resolution.
 * If screen width >= 860dp, all 3 columns fit side-by-side with zero horizontal scroll.
 * If narrower, it enables smooth horizontal scroll so GPU, NVMe, and Power are NEVER unreachable or stretched!
 * Each column has natural compact cards that don't stretch vertically ("alargados").
 */
@Composable
private fun LandscapeAdaptiveLayout(
    uiState: TelemetryUiState,
    availableWidth: Dp,
    availableHeight: Dp
) {
    val horizontalScrollState = rememberScrollState()

    // Determine target column width based on available screen space
    val targetColWidth = if (availableWidth >= 880.dp) {
        // Fits comfortably in 3 columns
        (availableWidth - 24.dp) / 3f
    } else {
        // Minimum comfortable readable width for dense hardware widgets
        275.dp
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
            .padding(bottom = 4.dp)
            .testTag("landscape_adaptive_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Column 1: Gaming HUD & Frametime Graph
        if (uiState.config.showGamingHud) {
            Column(
                modifier = Modifier
                    .width(targetColWidth)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GamingHudWidget(
                    gaming = uiState.snapshot.gaming,
                    frametimes = uiState.frametimes
                )
            }
        }

        // Column 2: GPU Diagnostics & CPU Diagnostics
        Column(
            modifier = Modifier
                .width(targetColWidth)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.config.showGpuCard) {
                GpuMetricsWidget(
                    gpu = uiState.snapshot.gpu,
                    useFahrenheit = uiState.config.useFahrenheit
                )
            }
            if (uiState.config.showCpuCard) {
                CpuMetricsWidget(
                    cpu = uiState.snapshot.cpu,
                    ram = uiState.snapshot.ram,
                    useFahrenheit = uiState.config.useFahrenheit
                )
            }
        }

        // Column 3: System Power Matrix & NVMe Storage / Network
        Column(
            modifier = Modifier
                .width(targetColWidth)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.config.showPowerCard) {
                PowerMatrixWidget(
                    power = uiState.snapshot.power,
                    powerHistory = uiState.powerHistory
                )
            }
            if (uiState.config.showNvmeCard) {
                NvmeStorageWidget(
                    nvme = uiState.snapshot.nvme,
                    net = uiState.snapshot.net,
                    useFahrenheit = uiState.config.useFahrenheit
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    status: ConnectionStatus,
    rateBadge: String,
    isLandscape: Boolean,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onOpenSetup: () -> Unit,
    onOpenConfig: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title + Status
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
            Spacer(modifier = Modifier.width(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "OMNIDECK",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ARCH/CACHY",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                    modifier = Modifier
                        .background(Color(0xFF0F1B2B), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when (status) {
                    is ConnectionStatus.Connected -> status.detail
                    is ConnectionStatus.Connecting -> status.message
                    is ConnectionStatus.Error -> status.message
                    ConnectionStatus.Disconnected -> "Disconnected"
                },
                fontSize = 8.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }

        // Action Buttons: Fullscreen toggle, Rate badge, Setup, Tuning
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Fullscreen / Immersive Mode button (hides status & navigation bar like games)
            Box(
                modifier = Modifier
                    .background(
                        if (isFullscreen) CyberEmerald.copy(alpha = 0.15f) else DarkStudioSurface,
                        RoundedCornerShape(5.dp)
                    )
                    .border(
                        1.dp,
                        if (isFullscreen) CyberEmerald else CardBorder,
                        RoundedCornerShape(5.dp)
                    )
                    .clickable { onToggleFullscreen() }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                    .testTag("fullscreen_toggle_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Pantalla Completa",
                        tint = if (isFullscreen) CyberEmerald else TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (isFullscreen) "INMERSIVO" else "BARRA",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFullscreen) CyberEmerald else TextMuted
                    )
                }
            }

            // Sampling rate badge (1s, 5s)
            Box(
                modifier = Modifier
                    .background(DarkStudioSurface, RoundedCornerShape(5.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(5.dp))
                    .clickable { onOpenConfig() }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "$rateBadge RATE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = VoltYellow
                )
            }

            // Linux Host Setup button
            IconButton(
                onClick = onOpenSetup,
                modifier = Modifier
                    .size(26.dp)
                    .testTag("linux_setup_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Linux Host Setup",
                    tint = CyberEmerald,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Customize & Tuning button
            IconButton(
                onClick = onOpenConfig,
                modifier = Modifier
                    .size(26.dp)
                    .testTag("customize_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Customize Dashboard",
                    tint = TextPrimary,
                    modifier = Modifier.size(17.dp)
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
