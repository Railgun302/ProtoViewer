package com.example.model

import org.json.JSONObject

data class CpuMetrics(
    val usagePercent: Float = 0f,
    val tempCelsius: Float = 0f,
    val powerWatts: Float = 0f,
    val freqMhz: Int = 0,
    val coreUsages: List<Float> = emptyList()
)

data class GpuMetrics(
    val name: String = "GPU",
    val usagePercent: Float = 0f,
    val tempCoreCelsius: Float = 0f,
    val tempHotspotCelsius: Float = 0f,
    val powerWatts: Float = 0f,
    val vramUsedMb: Int = 0,
    val vramTotalMb: Int = 0,
    val fanRpm: Int = 0
) {
    val vramPercent: Float
        get() = if (vramTotalMb > 0) (vramUsedMb.toFloat() / vramTotalMb * 100f).coerceIn(0f, 100f) else 0f
}

data class PowerMetrics(
    val totalWatts: Float = 0f,
    val cpuWatts: Float = 0f,
    val gpuWatts: Float = 0f,
    val peakWatts: Float = 0f
)

data class NvmeMetrics(
    val name: String = "NVMe SSD",
    val tempCelsius: Float = 0f,
    val readMbps: Float = 0f,
    val writeMbps: Float = 0f
) {
    val isThermalWarning: Boolean
        get() = tempCelsius >= 70f
}

data class GamingMetrics(
    val isGamingActive: Boolean = false,
    val fps: Float = 0f,
    val fps1PercentLow: Float = 0f,
    val frametimeMs: Float = 0f,
    val gameTitle: String = ""
)

data class RamMetrics(
    val usedMb: Int = 0,
    val totalMb: Int = 0
) {
    val usagePercent: Float
        get() = if (totalMb > 0) (usedMb.toFloat() / totalMb * 100f).coerceIn(0f, 100f) else 0f
}

data class NetworkMetrics(
    val downMbps: Float = 0f,
    val upMbps: Float = 0f
)

data class TelemetrySnapshot(
    val timestamp: Long = System.currentTimeMillis(),
    val cpu: CpuMetrics = CpuMetrics(),
    val gpu: GpuMetrics = GpuMetrics(),
    val power: PowerMetrics = PowerMetrics(),
    val nvme: NvmeMetrics = NvmeMetrics(),
    val ram: RamMetrics = RamMetrics(),
    val gaming: GamingMetrics = GamingMetrics(),
    val net: NetworkMetrics = NetworkMetrics(),
    val isSimulated: Boolean = false
) {
    companion object {
        fun fromJson(jsonStr: String): TelemetrySnapshot? {
            return try {
                val root = JSONObject(jsonStr)

                val cpuObj = root.optJSONObject("cpu")
                val coreList = mutableListOf<Float>()
                cpuObj?.optJSONArray("cores")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        coreList.add(arr.optDouble(i, 0.0).toFloat())
                    }
                }
                val cpu = CpuMetrics(
                    usagePercent = cpuObj?.optDouble("usage", 0.0)?.toFloat() ?: 0f,
                    tempCelsius = cpuObj?.optDouble("temp", 0.0)?.toFloat() ?: 0f,
                    powerWatts = cpuObj?.optDouble("power_watts", 0.0)?.toFloat() ?: 0f,
                    freqMhz = cpuObj?.optInt("freq_mhz", 0) ?: 0,
                    coreUsages = coreList
                )

                val gpuObj = root.optJSONObject("gpu")
                val gpu = GpuMetrics(
                    name = gpuObj?.optString("name", "GPU") ?: "GPU",
                    usagePercent = gpuObj?.optDouble("usage", 0.0)?.toFloat() ?: 0f,
                    tempCoreCelsius = gpuObj?.optDouble("temp_core", 0.0)?.toFloat() ?: 0f,
                    tempHotspotCelsius = gpuObj?.optDouble("temp_hotspot", 0.0)?.toFloat() ?: 0f,
                    powerWatts = gpuObj?.optDouble("power_watts", 0.0)?.toFloat() ?: 0f,
                    vramUsedMb = gpuObj?.optInt("vram_used_mb", 0) ?: 0,
                    vramTotalMb = gpuObj?.optInt("vram_total_mb", 0) ?: 0,
                    fanRpm = gpuObj?.optInt("fan_rpm", 0) ?: 0
                )

                val powerObj = root.optJSONObject("power")
                val totalWatts = powerObj?.optDouble("total_watts", 0.0)?.toFloat()
                    ?: (cpu.powerWatts + gpu.powerWatts)
                val power = PowerMetrics(
                    totalWatts = totalWatts,
                    cpuWatts = powerObj?.optDouble("cpu_watts", cpu.powerWatts.toDouble())?.toFloat() ?: cpu.powerWatts,
                    gpuWatts = powerObj?.optDouble("gpu_watts", gpu.powerWatts.toDouble())?.toFloat() ?: gpu.powerWatts,
                    peakWatts = powerObj?.optDouble("peak_watts", totalWatts.toDouble())?.toFloat() ?: totalWatts
                )

                val nvmeObj = root.optJSONObject("nvme")
                val nvme = NvmeMetrics(
                    name = nvmeObj?.optString("name", "NVMe SSD") ?: "NVMe SSD",
                    tempCelsius = nvmeObj?.optDouble("temp_c", 0.0)?.toFloat() ?: 0f,
                    readMbps = nvmeObj?.optDouble("disk_read_mbps", 0.0)?.toFloat() ?: 0f,
                    writeMbps = nvmeObj?.optDouble("disk_write_mbps", 0.0)?.toFloat() ?: 0f
                )

                val ramObj = root.optJSONObject("ram")
                val ram = RamMetrics(
                    usedMb = ramObj?.optInt("used_mb", 0) ?: 0,
                    totalMb = ramObj?.optInt("total_mb", 0) ?: 0
                )

                val gamingObj = root.optJSONObject("gaming")
                val gaming = GamingMetrics(
                    isGamingActive = gamingObj?.optBoolean("active", false) ?: false,
                    fps = gamingObj?.optDouble("fps", 0.0)?.toFloat() ?: 0f,
                    fps1PercentLow = gamingObj?.optDouble("fps_1percent", 0.0)?.toFloat() ?: 0f,
                    frametimeMs = gamingObj?.optDouble("frametime_ms", 0.0)?.toFloat() ?: 0f,
                    gameTitle = gamingObj?.optString("app_name", "") ?: ""
                )

                val netObj = root.optJSONObject("net")
                val net = NetworkMetrics(
                    downMbps = netObj?.optDouble("down_mbps", 0.0)?.toFloat() ?: 0f,
                    upMbps = netObj?.optDouble("up_mbps", 0.0)?.toFloat() ?: 0f
                )

                TelemetrySnapshot(
                    timestamp = root.optLong("timestamp", System.currentTimeMillis()),
                    cpu = cpu,
                    gpu = gpu,
                    power = power,
                    nvme = nvme,
                    ram = ram,
                    gaming = gaming,
                    net = net,
                    isSimulated = false
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
