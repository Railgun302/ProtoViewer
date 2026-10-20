# OmniDeck Hardware Monitor: Arch Linux to Moto Z USB Companion Display

A real-time, low-latency PC hardware telemetry dashboard designed for Linux (Arch/CachyOS) streaming directly to an Android companion screen (Moto Z Droid) over USB-C, with customizable widgets, smooth historical graphs, dark studio aesthetics, and variable refresh rates.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following design and architecture choices have been updated based on your feedback:

- **Confirmed Connection Mode**: Multi-mode engine supporting both direct **USB Serial CDC-ACM** (when connected via USB Host/OTG) and **ADB Reverse TCP Stream** (`adb reverse tcp:8088 tcp:8088` / socket stream over standard USB-C cable) as well as USB Tethering/Local LAN fallback.
- **Confirmed Aesthetic**: **Minimalist Dark Studio** with deep pitch-black AMOLED backgrounds (`#0B0E14` / `#000000`), subtle slate border cards (`#1E222D`), precision typography, and clean dual-accent neon indicators (Cyber Emerald `#00E5A3` for healthy states and Solar Amber/Crimson `#FF3366` for thermal alerts).
- **Confirmed Power & Wattage Engine (NEW)**:
  - **Intel & AMD CPU Wattage**: Native Linux **RAPL** (`/sys/class/powercap/intel-rapl/intel-rapl:0/energy_uj`) and AMD `k10temp` / `zenpower3` power sensors (`/sys/class/hwmon/hwmon*/power1_input`) natively included in CachyOS kernels.
  - **Nvidia & AMD GPU Wattage**: AMDGPU driver sysfs (`/sys/class/drm/card*/device/hwmon/hwmon*/power1_average` or `rocm-smi`) and Nvidia via `nvidia-smi` / `pynvml` reading real-time board power draw in Watts.
  - **Total System Power Gauge**: Dynamic aggregate calculating combined CPU + GPU + peripheral wattage.
- **Confirmed NVMe Thermals & Drive Health (NEW)**:
  - NVMe thermal monitoring via native Linux `nvme-hwmon` (`/sys/class/hwmon/hwmon*/temp1_input` with sensor label `Composite` / `Sensor 1` / `Sensor 2`), paired with disk I/O throughput in MB/s.
- **Confirmed Gaming & Telemetry Priorities**:
  - MangoHud integration: Real-time FPS, 1% Low FPS, and Frametime graph.
  - GPU telemetry: Core & Hotspot temperatures, Wattage, utilization %, VRAM usage, and fan speeds.
  - CPU telemetry: Overall usage %, package temperature, Wattage, average frequency GHz, and RAM usage.
  - Storage & Network: NVMe temperatures, read/write speeds, and live network throughput.

---

## 1. Overview & Core Concept

**OmniDeck** transforms an Android phone (such as the 1440p AMOLED Moto Z Droid) into an ultra-low latency, dedicated desktop hardware monitor positioned below or beside the primary monitor. 

### Target Audience & Persona
PC gamers, performance enthusiasts, and developers running Arch Linux / CachyOS who need continuous, distraction-free performance statistics, wattage monitoring, thermal awareness, and in-game framerate monitoring without intrusive on-screen overlays or CPU-heavy web wrappers.

### Key Value
- **Deep Linux Hardware Probing**: Direct reads of Intel/AMD CPU Wattage, Nvidia/AMD GPU Wattage, and NVMe temperatures with zero third-party bloat.
- **Zero-Flicker Hardware Telemetry**: Sub-10ms data refresh over high-speed USB-C.
- **AMOLED-Optimized Dark Studio UI**: True black backgrounds to eliminate screen burn-in and save power on the Moto Z.
- **Total Customization**: Modular widget grid, toggleable graph spans (30s, 60s, 120s), custom wallpapers with adjustable dimming, and adjustable refresh rates (1 Hz, 2 Hz, 5 Hz, 10 Hz).
- **Built-in Daemon Generator & Simulation Mode**: App includes a one-tap daemon export/copy screen and an interactive live simulator so the user can preview, test, and style widgets right away.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌────────────────────────────────────────────────────────────────────────┐
│                          USER FLOW PIPELINE                            │
│                                                                        │
│  [First Launch] ──> [Live Demo Mode Active] ──> [Explore Customization] │
│                             │                            │             │
│                             ▼                            ▼             │
│                 [Plug USB-C to Arch PC]        [Choose Studio Themes,  │
│                             │                   Wallpapers & Widgets]  │
│                             ▼                                          │
│                 [Copy 1-Line Daemon Script]                            │
│                 (pacman -S lm_sensors python)                          │
│                             │                                          │
│                             ▼                                          │
│           [OmniDeck Syncs & Displays Telemetry] ◄──────────────────────┘
└────────────────────────────────────────────────────────────────────────┘
```

1. **Dashboard Home**: Fullscreen dashboard displaying prominent modular widget sections:
   - **Gaming HUD**: Big framerate readout (Current FPS, 1% Lows, 0.1% Lows), interactive frametime line-graph.
   - **GPU Command Card**: Core temperature gauge, Hotspot temperature, **Real-time Wattage (W)**, VRAM bar meter, GPU clock, and fan speed RPM.
   - **CPU Core Card**: Total usage percentage, package temperature, **Real-time Wattage (W)**, average frequency GHz, and RAM usage.
   - **Power Matrix Card (NEW)**: Combined Live Power Bar (CPU W + GPU W = Total W), peak power tracker, and efficiency indicator.
   - **Storage & I/O Card**: **NVMe Temperature (°C)** with thermal alert threshold (>70°C warning badge), Read/Write speed (MB/s), and Network upload/download bandwidth.
2. **Customization Tray & Sheet**:
   - **Refresh Rate Selector**: 10 Hz (Gaming ultra-smooth), 5 Hz (Balanced), 2 Hz (Battery saver), 1 Hz (Static monitoring).
   - **Widget Layout Customizer**: Rearrange or toggle visibility for MangoHud, GPU, CPU, Power Draw, NVMe Thermals, Network, or Clock tiles.
   - **Wallpaper & Visual Styling**: AMOLED Pure Black, Carbon Weave, Studio Slate, or custom color wash with backdrop dimming and blur controls.
   - **Alert Thresholds**: Configurable high-temperature & high-wattage warning badges (e.g. GPU > 80°C or NVMe > 70°C turns amber/red).
3. **Connection & Daemon Manager**:
   - Shows connection status: `Connected (USB Serial)`, `Connected (ADB/TCP:8088)`, or `Demo Simulation Mode`.
   - Dedicated Linux Quick-Start tab with syntax-highlighted Arch/CachyOS commands, daemon source viewer, and one-tap clipboard copy.

### Visual Identity & Theme Tokens
- **Design Language**: Minimalist Dark Studio (M3 with pitch-black surfaces, razor-sharp status rings, and soft card elevation).
- **Color Palette**:
  - Background: Pure Black `#000000` / Midnight `#0A0D14`
  - Surface Card: Slate Studio `#121620` with subtle border stroke `#1F2633`
  - Accent Gaming / Primary: Emerald Neon `#00E5A3`
  - Accent GPU & Power: Electric Cyan `#00B4D8` / Volt Yellow `#E0FB51`
  - Accent Alerts / Thermals: Hyper Amber `#FFB703` / Laser Coral `#FF3366`
  - Text Primary: Studio White `#F1F5F9`, Secondary `#94A3B8`, Tertiary `#64748B`
- **Typography**: Clean, monospace telemetry figures paired with compact, high-legibility sans headers.

---

## 3. Key Product Decisions & Trade-Offs

### 1. Dual-Protocol Engine (USB Serial CDC + ADB/TCP Stream)
- *Chosen Approach*: Implement a unified input listener that automatically handles:
  1. USB Host Serial CDC-ACM (using Android's native `UsbManager` and CDC protocol parsing).
  2. TCP socket stream on port `8088` (which connects seamlessly with standard USB-C data cables using `adb reverse tcp:8088 tcp:8088` or USB tethering).
- *Why*: While USB Serial Host mode requires a USB OTG adapter/host role, connecting a phone directly to a PC usually operates in USB Device/ADB mode. Supporting both ensures 100% plug-and-play compatibility on any cable configuration without extra hardware.

### 2. Linux Hardware Probing (Intel/AMD Wattage, Nvidia/AMD GPU Power, NVMe Temps)
- *Chosen Approach*: The standalone Python daemon script directly leverages standard Linux interfaces without requiring third-party compiled binaries:
  - **Intel CPU Power**: Reads `/sys/class/powercap/intel-rapl/intel-rapl:0/energy_uj` over delta time ($\Delta \text{Energy} / \Delta t = \text{Watts}$) or `/sys/class/hwmon/hwmon*/power1_input`.
  - **AMD CPU Power**: Reads `/sys/class/hwmon/hwmon*/power1_average` or `k10temp`/`zenpower3` MSR attributes.
  - **Nvidia GPU Power & Thermals**: Invokes NVML directly via Python ctypes or `nvidia-smi` queries.
  - **AMD GPU Power & Thermals**: Reads `/sys/class/drm/card0/device/hwmon/hwmon*/power1_average` (micro-watts to Watts) and `temp1_input` / `temp2_input`.
  - **NVMe Temperatures**: Reads `/sys/class/hwmon/hwmon*/temp1_input` where the hwmon name is `nvme`.
  - **MangoHud**: Reads MangoHud shared socket or `/dev/shm` IPC for active game FPS/frametimes.
- *Why*: Arch and CachyOS kernels expose these hardware monitors natively. This keeps CPU usage on the host PC under 0.1% while providing true sub-second hardware metrics.

### 3. Native Canvas Waveform & Graph Engine
- *Chosen Approach*: Custom Jetpack Compose `Canvas` sparklines and smooth cubic Bézier charts for Frametime and CPU/GPU history instead of heavy third-party web chart bridges.
- *Why*: Native Compose canvas renders at a locked 60/120 Hz with microsecond draw times and zero garbage-collection overhead, keeping the Moto Z cool and responsive.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                       ARCH LINUX / CACHYOS PC                          │
│                                                                        │
│  [Intel RAPL / AMD Zenpower]   [Nvidia NVML / AMD hwmon]   [NVMe hwmon]│
│         │ (CPU Watts)                  │ (GPU Watts)             │     │
│         └───────────────┬──────────────┴─────────────────────────┘     │
│                         ▼                                              │
│               [omnideck_daemon.py] ◄── [MangoHud IPC (FPS/Frametime)]  │
│                         │                                              │
│            ┌────────────┴────────────┐                                 │
│            ▼                         ▼                                 │
│      [/dev/ttyACM0]           [TCP Port 8088]                          │
└────────────┬─────────────────────────┬─────────────────────────────────┘
             │ USB-C Cable             │ adb reverse / socket
             ▼                         ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     MOTO Z DROID (ANDROID APP)                         │
│                                                                        │
│      [UsbSerialDriver]               [TcpStreamListener]               │
│             │                                 │                        │
│             └────────────────┬────────────────┘                        │
│                              ▼                                         │
│                  [TelemetryDataParser]                                 │
│                              │                                         │
│                              ▼                                         │
│                  [TelemetryViewModel]                                  │
│                (StateFlow<TelemetrySnapshot>)                          │
│                              │                                         │
│       ┌──────────────────────┼──────────────────────┐                  │
│       ▼                      ▼                      ▼                  │
│ [GamingHudCard]       [GpuMetricsCard]       [PowerMatrixCard]         │
│ (FPS/Frametime Graph) (Temp/Watts/VRAM)      (CPU+GPU Live Watts)      │
│       │                      │                      │                  │
│       └──────────────────────┼──────────────────────┘                  │
│                              ▼                                         │
│                      [NvmeStorageCard]                                 │
│                   (NVMe Temp/MBps & Net)                               │
└────────────────────────────────────────────────────────────────────────┘
```

### JSON Telemetry Contract

```json
{
  "timestamp": 1718000000,
  "cpu": {
    "usage": 34.2,
    "temp": 58.0,
    "power_watts": 65.4,
    "freq_mhz": 4650,
    "cores": [32.0, 45.1, 28.0, 31.5]
  },
  "gpu": {
    "name": "AMD Radeon RX 7900 XTX",
    "usage": 87.5,
    "temp_core": 62.0,
    "temp_hotspot": 74.0,
    "power_watts": 285.0,
    "vram_used_mb": 9420,
    "vram_total_mb": 24576,
    "fan_rpm": 1420
  },
  "power": {
    "total_watts": 350.4,
    "cpu_watts": 65.4,
    "gpu_watts": 285.0
  },
  "nvme": {
    "temp_c": 48.0,
    "disk_read_mbps": 42.5,
    "disk_write_mbps": 18.2
  },
  "ram": {
    "used_mb": 14200,
    "total_mb": 32768
  },
  "gaming": {
    "active": true,
    "fps": 144.2,
    "fps_1percent": 118.0,
    "frametime_ms": 6.93,
    "app_name": "Cyberpunk 2077"
  },
  "net": {
    "down_mbps": 12.4,
    "up_mbps": 1.8
  }
}
```

### Component Hierarchy & Module Breakdown
1. **Core Domain & Models**:
   - `TelemetryData.kt`: Models for CPU, GPU, Power (Watts), NVMe (Temp & MB/s), Gaming (FPS/Frametimes), and Network.
   - `MetricHistoryBuffer.kt`: High-efficiency ring buffer storing historical data points for smooth line sparklines.
   - `DisplayConfig.kt`: Preferences for refresh rate (1Hz - 10Hz), active widgets, dark studio wallpapers, and alert limits.
2. **Communication Layer**:
   - `UsbSerialManager.kt`: Native Android USB Host API integration for CDC-ACM devices.
   - `TcpTelemetryServer.kt`: Embedded Coroutine socket listener bound to `0.0.0.0:8088`.
   - `SimulationEngine.kt`: Built-in realistic hardware simulator generating dynamic gaming load, fluctuating wattage (50W - 400W), thermal curves, and NVMe activity for offline demonstration.
3. **UI & Dashboard Components**:
   - `TelemetryDashboardScreen.kt`: Responsive AMOLED dashboard with adaptive landscape and portrait layouts.
   - `GamingHudWidget.kt`: Framerate ticker, 1% low metrics, and real-time Canvas frametime graph.
   - `GpuMetricsWidget.kt`: Core & Hotspot thermals, GPU Wattage meter, VRAM gauge, and Fan RPM.
   - `CpuMetricsWidget.kt`: CPU utilization %, Package Temp, CPU Wattage, and Clock frequencies.
   - `PowerMatrixWidget.kt`: Dynamic system power readout (CPU W + GPU W = Total W) with load bar.
   - `NvmeStorageWidget.kt`: NVMe thermal gauge with high-temp alert badge and read/write bandwidth.
   - `LinuxSetupSheet.kt`: Integrated Arch/CachyOS daemon installer view with copyable Python script, instructions for Intel RAPL, AMD zenpower, Nvidia/AMD wattage, and `adb reverse` command.
   - `AppearanceSettingsDialog.kt`: Refresh rate tuning, wallpaper selector, and AMOLED burn-in protection shifts.

---

## Next Steps Upon Approval
Once approved, we will build the complete production codebase with:
1. Android Manifest configuration (USB Host permissions, Internet socket permissions).
2. Complete multi-mode communication layer (USB CDC + TCP + Realistic Simulator).
3. Linux daemon script generator embedded into the app with full support for Intel/AMD CPU Wattage, Nvidia/AMD GPU Wattage, and NVMe temperatures.
4. Minimalist Dark Studio Compose UI with modular cards, graphs, and settings.
5. Robolectric unit verification.
