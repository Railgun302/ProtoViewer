package com.example.connection

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import com.example.model.RefreshRate
import com.example.model.TelemetrySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class TelemetryConnectionManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var workerJob: Job? = null

    private val _status = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

    private val _currentSnapshot = MutableStateFlow(TelemetrySnapshot())
    val currentSnapshot: StateFlow<TelemetrySnapshot> = _currentSnapshot.asStateFlow()

    private val simulationEngine = SimulationEngine()
    private var activeMode: ConnectionMode = ConnectionMode.AUTO
    private var refreshRate: RefreshRate = RefreshRate.BALANCED

    fun start(mode: ConnectionMode = ConnectionMode.AUTO, rate: RefreshRate = RefreshRate.BALANCED) {
        this.activeMode = mode
        this.refreshRate = rate
        workerJob?.cancel()
        workerJob = scope.launch {
            runConnectionLoop()
        }
    }

    fun updateRefreshRate(newRate: RefreshRate) {
        this.refreshRate = newRate
    }

    fun switchMode(newMode: ConnectionMode) {
        this.activeMode = newMode
        workerJob?.cancel()
        workerJob = scope.launch {
            runConnectionLoop()
        }
    }

    private suspend fun CoroutineScope.runConnectionLoop() {
        while (isActive) {
            when (activeMode) {
                ConnectionMode.DEMO -> {
                    runDemoStream()
                }
                ConnectionMode.ADB_TCP -> {
                    val connected = tryConnectAdbTcp()
                    if (!connected) {
                        delay(1500)
                    }
                }
                ConnectionMode.USB_SERIAL -> {
                    val connected = tryConnectUsbSerial()
                    if (!connected) {
                        delay(2000)
                    }
                }
                ConnectionMode.AUTO -> {
                    // 1. Try USB Serial
                    val usbOk = tryConnectUsbSerial()
                    if (usbOk) continue

                    // 2. Try TCP (ADB reverse or USB Tethering)
                    val tcpOk = tryConnectAdbTcp()
                    if (tcpOk) continue

                    // 3. If neither is active, run smooth demo stream while re-checking every few seconds
                    _status.value = ConnectionStatus.Connected(
                        ConnectionMode.DEMO,
                        "Live Demo Mode (Awaiting PC Stream on Port 8088 / USB)"
                    )
                    var ticks = 0
                    while (isActive && ticks < 20) {
                        _currentSnapshot.value = simulationEngine.nextSnapshot()
                        delay(refreshRate.intervalMs)
                        ticks++
                    }
                }
            }
        }
    }

    private suspend fun CoroutineScope.runDemoStream() {
        _status.value = ConnectionStatus.Connected(ConnectionMode.DEMO, "Simulated Cyberpunk 2077 Benchmark")
        while (isActive) {
            _currentSnapshot.value = simulationEngine.nextSnapshot()
            delay(refreshRate.intervalMs)
        }
    }

    private suspend fun CoroutineScope.tryConnectAdbTcp(): Boolean {
        _status.value = ConnectionStatus.Connecting("Checking TCP stream (adb reverse 127.0.0.1:8088)...")
        var socket: Socket? = null
        try {
            socket = Socket()
            // Try connecting to localhost:8088 (forwarded via adb reverse)
            socket.connect(InetSocketAddress("127.0.0.1", 8088), 1200)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            _status.value = ConnectionStatus.Connected(ConnectionMode.ADB_TCP, "Connected via ADB Stream (127.0.0.1:8088)")

            while (isActive) {
                val line = reader.readLine() ?: break
                val snapshot = TelemetrySnapshot.fromJson(line)
                if (snapshot != null) {
                    _currentSnapshot.value = snapshot
                }
            }
            return true
        } catch (e: Exception) {
            // TCP connect failed
            return false
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    private suspend fun CoroutineScope.tryConnectUsbSerial(): Boolean {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return false
        val deviceList = usbManager.deviceList
        if (deviceList.isEmpty()) return false

        for (device in deviceList.values) {
            // Look for CDC-ACM or USB Serial interface
            val connection = usbManager.openDevice(device) ?: continue
            val (iface, inEndpoint) = findCdcEndpoints(device) ?: run {
                connection.close()
                null
            } ?: continue

            connection.claimInterface(iface, true)
            _status.value = ConnectionStatus.Connected(
                ConnectionMode.USB_SERIAL,
                "USB CDC: ${device.productName ?: "Serial Device"}"
            )

            val buffer = ByteArray(2048)
            val lineBuilder = StringBuilder()

            try {
                while (isActive) {
                    val bytesRead = connection.bulkTransfer(inEndpoint, buffer, buffer.size, 500)
                    if (bytesRead > 0) {
                        val chunk = String(buffer, 0, bytesRead, Charsets.UTF_8)
                        lineBuilder.append(chunk)
                        val newlineIdx = lineBuilder.indexOf('\n')
                        if (newlineIdx >= 0) {
                            val line = lineBuilder.substring(0, newlineIdx).trim()
                            lineBuilder.delete(0, newlineIdx + 1)
                            val snapshot = TelemetrySnapshot.fromJson(line)
                            if (snapshot != null) {
                                _currentSnapshot.value = snapshot
                            }
                        }
                    }
                }
                return true
            } catch (e: Exception) {
                return false
            } finally {
                connection.releaseInterface(iface)
                connection.close()
            }
        }
        return false
    }

    private fun findCdcEndpoints(device: UsbDevice): Pair<UsbInterface, UsbEndpoint>? {
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            // CDC Data class (0x0A) or vendor specific
            for (j in 0 until iface.endpointCount) {
                val ep = iface.getEndpoint(j)
                if (ep.direction == UsbConstants.USB_DIR_IN && ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    return Pair(iface, ep)
                }
            }
        }
        return null
    }

    fun stop() {
        workerJob?.cancel()
        _status.value = ConnectionStatus.Disconnected
    }
}
