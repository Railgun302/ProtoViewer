package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.connection.SimulationEngine
import com.example.model.MetricHistoryBuffer
import com.example.model.TelemetrySnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OmniDeck", appName)
    }

    @Test
    fun `parse telemetry json snapshot with wattage and nvme`() {
        val json = """
            {
                "timestamp": 1718000000,
                "cpu": {
                    "usage": 45.2,
                    "temp": 62.5,
                    "power_watts": 85.0,
                    "freq_mhz": 4800,
                    "cores": [40.0, 50.0]
                },
                "gpu": {
                    "name": "AMD Radeon RX 7900 XTX",
                    "usage": 88.0,
                    "temp_core": 64.0,
                    "temp_hotspot": 76.0,
                    "power_watts": 290.0,
                    "vram_used_mb": 8192,
                    "vram_total_mb": 24576,
                    "fan_rpm": 1500
                },
                "power": {
                    "total_watts": 405.0,
                    "cpu_watts": 85.0,
                    "gpu_watts": 290.0
                },
                "nvme": {
                    "name": "Samsung 990 PRO",
                    "temp_c": 52.0,
                    "disk_read_mbps": 120.0,
                    "disk_write_mbps": 45.0
                },
                "gaming": {
                    "active": true,
                    "fps": 143.5,
                    "fps_1percent": 115.0,
                    "frametime_ms": 6.96,
                    "app_name": "Cyberpunk 2077"
                }
            }
        """.trimIndent()

        val snapshot = TelemetrySnapshot.fromJson(json)
        assertNotNull(snapshot)
        snapshot?.let {
            assertEquals(45.2f, it.cpu.usagePercent, 0.01f)
            assertEquals(85.0f, it.cpu.powerWatts, 0.01f)
            assertEquals(290.0f, it.gpu.powerWatts, 0.01f)
            assertEquals(405.0f, it.power.totalWatts, 0.01f)
            assertEquals(52.0f, it.nvme.tempCelsius, 0.01f)
            assertEquals(143.5f, it.gaming.fps, 0.01f)
            assertEquals("Cyberpunk 2077", it.gaming.gameTitle)
        }
    }

    @Test
    fun `simulation engine generates realistic metrics`() {
        val engine = SimulationEngine()
        val snapshot = engine.nextSnapshot()
        assertNotNull(snapshot)
        assertTrue(snapshot.cpu.usagePercent > 0f)
        assertTrue(snapshot.cpu.powerWatts > 0f)
        assertTrue(snapshot.gpu.powerWatts > 0f)
        assertTrue(snapshot.power.totalWatts > 0f)
        assertTrue(snapshot.nvme.tempCelsius > 0f)
        assertTrue(snapshot.gaming.fps > 0f)
    }

    @Test
    fun `metric history buffer handles roll correctly`() {
        val buffer = MetricHistoryBuffer(capacity = 5)
        buffer.add(10f)
        buffer.add(20f)
        buffer.add(30f)
        assertEquals(3, buffer.toList().size)
        buffer.add(40f)
        buffer.add(50f)
        buffer.add(60f)
        val list = buffer.toList()
        assertEquals(5, list.size)
        assertEquals(20f, list.first(), 0.01f)
        assertEquals(60f, list.last(), 0.01f)
    }
}
