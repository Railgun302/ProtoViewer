package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.connection.ConnectionMode
import com.example.connection.ConnectionStatus
import com.example.connection.TelemetryConnectionManager
import com.example.model.DisplayConfig
import com.example.model.MetricHistoryBuffer
import com.example.model.RefreshRate
import com.example.model.StudioWallpaper
import com.example.model.TelemetrySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TelemetryUiState(
    val snapshot: TelemetrySnapshot = TelemetrySnapshot(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val config: DisplayConfig = DisplayConfig(),
    val frametimes: List<Float> = emptyList(),
    val cpuHistory: List<Float> = emptyList(),
    val gpuHistory: List<Float> = emptyList(),
    val powerHistory: List<Float> = emptyList(),
    val isSetupSheetVisible: Boolean = false,
    val isConfigSheetVisible: Boolean = false,
    val pixelShiftX: Float = 0f,
    val pixelShiftY: Float = 0f
)

class TelemetryViewModel(application: Application) : AndroidViewModel(application) {

    private val connectionManager = TelemetryConnectionManager(application.applicationContext)

    private val frametimeBuffer = MetricHistoryBuffer(capacity = 60)
    private val cpuBuffer = MetricHistoryBuffer(capacity = 40)
    private val gpuBuffer = MetricHistoryBuffer(capacity = 40)
    private val powerBuffer = MetricHistoryBuffer(capacity = 40)

    private val _uiState = MutableStateFlow(TelemetryUiState())
    val uiState: StateFlow<TelemetryUiState> = _uiState.asStateFlow()

    private var tickCount = 0L

    init {
        // Start connection manager
        connectionManager.start(ConnectionMode.AUTO, _uiState.value.config.refreshRate)

        // Observe connection status
        viewModelScope.launch {
            connectionManager.status.collect { status ->
                _uiState.update { it.copy(connectionStatus = status) }
            }
        }

        // Observe telemetry updates
        viewModelScope.launch {
            connectionManager.currentSnapshot.collect { snapshot ->
                tickCount++

                // Add to buffers
                frametimeBuffer.add(
                    if (snapshot.gaming.frametimeMs > 0f) snapshot.gaming.frametimeMs
                    else (1000f / maxOf(1f, snapshot.gaming.fps))
                )
                cpuBuffer.add(snapshot.cpu.usagePercent)
                gpuBuffer.add(snapshot.gpu.usagePercent)
                powerBuffer.add(snapshot.power.totalWatts)

                // Periodic AMOLED pixel shift for burn-in prevention on Moto Z
                val (shiftX, shiftY) = if (_uiState.value.config.amoledShiftEnabled) {
                    val phase = (tickCount / 100) % 4
                    when (phase) {
                        0L -> Pair(0f, 0f)
                        1L -> Pair(1.5f, 0f)
                        2L -> Pair(1.5f, 1.5f)
                        else -> Pair(0f, 1.5f)
                    }
                } else {
                    Pair(0f, 0f)
                }

                _uiState.update { current ->
                    current.copy(
                        snapshot = snapshot,
                        frametimes = frametimeBuffer.toList(),
                        cpuHistory = cpuBuffer.toList(),
                        gpuHistory = gpuBuffer.toList(),
                        powerHistory = powerBuffer.toList(),
                        pixelShiftX = shiftX,
                        pixelShiftY = shiftY
                    )
                }
            }
        }
    }

    fun setRefreshRate(rate: RefreshRate) {
        _uiState.update { it.copy(config = it.config.copy(refreshRate = rate)) }
        connectionManager.updateRefreshRate(rate)
    }

    fun setWallpaper(wallpaper: StudioWallpaper) {
        _uiState.update { it.copy(config = it.config.copy(wallpaper = wallpaper)) }
    }

    fun toggleWidget(widgetKey: String) {
        _uiState.update { current ->
            val cfg = current.config
            val updated = when (widgetKey) {
                "gaming" -> cfg.copy(showGamingHud = !cfg.showGamingHud)
                "gpu" -> cfg.copy(showGpuCard = !cfg.showGpuCard)
                "cpu" -> cfg.copy(showCpuCard = !cfg.showCpuCard)
                "power" -> cfg.copy(showPowerCard = !cfg.showPowerCard)
                "nvme" -> cfg.copy(showNvmeCard = !cfg.showNvmeCard)
                "net" -> cfg.copy(showNetworkCard = !cfg.showNetworkCard)
                else -> cfg
            }
            current.copy(config = updated)
        }
    }

    fun setConnectionMode(mode: ConnectionMode) {
        connectionManager.switchMode(mode)
    }

    fun toggleAmoledShift() {
        _uiState.update { it.copy(config = it.config.copy(amoledShiftEnabled = !it.config.amoledShiftEnabled)) }
    }

    fun toggleScreenAwake() {
        _uiState.update { it.copy(config = it.config.copy(keepScreenAwake = !it.config.keepScreenAwake)) }
    }

    fun toggleUnit() {
        _uiState.update { it.copy(config = it.config.copy(useFahrenheit = !it.config.useFahrenheit)) }
    }

    fun toggleForceLandscapeFit() {
        _uiState.update { it.copy(config = it.config.copy(forceLandscapeFit = !it.config.forceLandscapeFit)) }
    }

    fun showSetupSheet(show: Boolean) {
        _uiState.update { it.copy(isSetupSheetVisible = show) }
    }

    fun showConfigSheet(show: Boolean) {
        _uiState.update { it.copy(isConfigSheetVisible = show) }
    }

    override fun onCleared() {
        super.onCleared()
        connectionManager.stop()
    }
}
