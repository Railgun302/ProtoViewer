package com.example.model

enum class RefreshRate(
    val seconds: Float,
    val intervalMs: Long,
    val label: String,
    val badge: String
) {
    ONE_SEC(1.0f, 1000L, "1 sec (Recomendado - 1 Hz)", "1s"),
    FIVE_SEC(5.0f, 5000L, "5 sec (Ultra Ahorro CPU/ADB)", "5s"),
    TWO_SEC(2.0f, 2000L, "2 sec (Equilibrado)", "2s"),
    FAST(0.5f, 500L, "0.5 sec (Rápido - 2 Hz)", "0.5s")
}

enum class StudioWallpaper(val id: String, val title: String, val description: String) {
    AMOLED_BLACK("amoled", "AMOLED Pitch Black", "True 0-watt black for Moto Z AMOLED screen"),
    STUDIO_CARBON("carbon", "Carbon Studio", "Subtle diagonal carbon weave texture"),
    CYBER_HEX("hex", "Cyber Hex Grid", "Faint isometric wireframe grid lines"),
    SLATE_DEEP("slate", "Deep Slate", "Minimal modern workstation gradient")
}

data class DisplayConfig(
    val refreshRate: RefreshRate = RefreshRate.ONE_SEC,
    val wallpaper: StudioWallpaper = StudioWallpaper.AMOLED_BLACK,
    val useFahrenheit: Boolean = false,
    val keepScreenAwake: Boolean = true,
    val amoledShiftEnabled: Boolean = true,
    val forceLandscapeFit: Boolean = true,
    val showGamingHud: Boolean = true,
    val showGpuCard: Boolean = true,
    val showCpuCard: Boolean = true,
    val showPowerCard: Boolean = true,
    val showNvmeCard: Boolean = true,
    val showNetworkCard: Boolean = true
)
