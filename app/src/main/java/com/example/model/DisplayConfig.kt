package com.example.model

enum class RefreshRate(val hz: Int, val intervalMs: Long, val label: String) {
    ULTRA(10, 100L, "10 Hz (Ultra Smooth)"),
    BALANCED(5, 200L, "5 Hz (Balanced)"),
    SAVER(2, 500L, "2 Hz (Low Power)"),
    STATIC(1, 1000L, "1 Hz (Minimalist)")
}

enum class StudioWallpaper(val id: String, val title: String, val description: String) {
    AMOLED_BLACK("amoled", "AMOLED Pitch Black", "True 0-watt black for Moto Z AMOLED screen"),
    STUDIO_CARBON("carbon", "Carbon Studio", "Subtle diagonal carbon weave texture"),
    CYBER_HEX("hex", "Cyber Hex Grid", "Faint isometric wireframe grid lines"),
    SLATE_DEEP("slate", "Deep Slate", "Minimal modern workstation gradient")
}

data class DisplayConfig(
    val refreshRate: RefreshRate = RefreshRate.BALANCED,
    val wallpaper: StudioWallpaper = StudioWallpaper.AMOLED_BLACK,
    val useFahrenheit: Boolean = false,
    val keepScreenAwake: Boolean = true,
    val amoledShiftEnabled: Boolean = true,
    val showGamingHud: Boolean = true,
    val showGpuCard: Boolean = true,
    val showCpuCard: Boolean = true,
    val showPowerCard: Boolean = true,
    val showNvmeCard: Boolean = true,
    val showNetworkCard: Boolean = true
)
