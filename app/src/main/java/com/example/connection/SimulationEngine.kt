package com.example.connection

import com.example.model.CpuMetrics
import com.example.model.GamingMetrics
import com.example.model.GpuMetrics
import com.example.model.NetworkMetrics
import com.example.model.NvmeMetrics
import com.example.model.PowerMetrics
import com.example.model.RamMetrics
import com.example.model.TelemetrySnapshot
import kotlin.math.sin

class SimulationEngine {
    private var tick = 0L

    fun nextSnapshot(): TelemetrySnapshot {
        tick++
        val t = tick * 0.15

        // Sine wave variations for organic load simulation
        val loadWave = (sin(t * 0.4) * 0.5 + 0.5).toFloat()
        val spikeWave = if (tick % 45 in 0..4) 1.0f else 0.0f

        // CPU Simulation (e.g. AMD Ryzen 7 7800X3D or Intel Core i7-14700K)
        val cpuUsage = (28f + loadWave * 38f + spikeWave * 20f).coerceIn(10f, 98f)
        val cpuTemp = (52f + loadWave * 18f + spikeWave * 8f).coerceIn(40f, 88f)
        val cpuWatts = (45f + loadWave * 42f + spikeWave * 28f).coerceIn(25f, 135f)
        val cpuFreq = (4200 + (loadWave * 750).toInt()).coerceIn(3600, 5050)
        val cores = List(8) { i ->
            (cpuUsage + (sin(t + i) * 12.0).toFloat()).coerceIn(5f, 100f)
        }
        val cpu = CpuMetrics(
            usagePercent = cpuUsage,
            tempCelsius = cpuTemp,
            powerWatts = cpuWatts,
            freqMhz = cpuFreq,
            coreUsages = cores
        )

        // GPU Simulation (e.g. NVIDIA RTX 4080 / AMD RX 7900 XTX)
        val gpuUsage = (72f + loadWave * 24f).coerceIn(40f, 100f)
        val gpuTempCore = (58f + loadWave * 14f).coerceIn(45f, 79f)
        val gpuTempHotspot = gpuTempCore + 11f + (spikeWave * 4f)
        val gpuWatts = (180f + loadWave * 115f + spikeWave * 25f).coerceIn(60f, 320f)
        val vramUsed = (10240 + (loadWave * 3200).toInt()).coerceIn(8000, 16384)
        val fanRpm = (1100 + (loadWave * 550).toInt()).coerceIn(800, 2000)
        val gpu = GpuMetrics(
            name = "GeForce RTX 4080 Super",
            usagePercent = gpuUsage,
            tempCoreCelsius = gpuTempCore,
            tempHotspotCelsius = gpuTempHotspot,
            powerWatts = gpuWatts,
            vramUsedMb = vramUsed,
            vramTotalMb = 16384,
            fanRpm = fanRpm
        )

        // Total System Power
        val totalWatts = cpuWatts + gpuWatts + 32f // Motherboard + fans + RAM
        val power = PowerMetrics(
            totalWatts = totalWatts,
            cpuWatts = cpuWatts,
            gpuWatts = gpuWatts,
            peakWatts = 365f
        )

        // NVMe Gen4 SSD Simulation
        val nvmeTemp = (46f + (loadWave * 8f)).coerceIn(38f, 64f)
        val nvmeRead = if (spikeWave > 0f) (380f + loadWave * 120f) else (12f + loadWave * 28f)
        val nvmeWrite = (4f + loadWave * 18f)
        val nvme = NvmeMetrics(
            name = "Samsung 990 PRO 2TB",
            tempCelsius = nvmeTemp,
            readMbps = nvmeRead,
            writeMbps = nvmeWrite
        )

        // RAM Simulation
        val ram = RamMetrics(
            usedMb = (15600 + (loadWave * 2400).toInt()),
            totalMb = 32768
        )

        // Gaming (MangoHud simulation: Cyberpunk 2077 @ 1440p DLSS Quality)
        val targetFps = 144f
        val fpsJitter = (sin(t * 1.5) * 8f).toFloat()
        val dip = if (spikeWave > 0f) -28f else 0f
        val fps = (targetFps + fpsJitter + dip).coerceIn(60f, 165f)
        val fps1Percent = (fps * 0.82f).coerceIn(45f, 130f)
        val frametime = (1000f / fps) + (if (spikeWave > 0f) 5.8f else 0f)

        val gaming = GamingMetrics(
            isGamingActive = true,
            fps = fps,
            fps1PercentLow = fps1Percent,
            frametimeMs = frametime,
            gameTitle = "Cyberpunk 2077 (Arch Linux)"
        )

        // Network
        val netDown = (18f + sin(t * 0.8) * 14f).coerceAtLeast(0.5).toFloat()
        val netUp = (2.2f + sin(t * 0.4) * 1.5f).coerceAtLeast(0.1).toFloat()
        val net = NetworkMetrics(downMbps = netDown, upMbps = netUp)

        return TelemetrySnapshot(
            timestamp = System.currentTimeMillis(),
            cpu = cpu,
            gpu = gpu,
            power = power,
            nvme = nvme,
            ram = ram,
            gaming = gaming,
            net = net,
            isSimulated = true
        )
    }
}
