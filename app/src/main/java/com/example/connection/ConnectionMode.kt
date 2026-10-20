package com.example.connection

enum class ConnectionMode(val title: String) {
    AUTO("Auto Detect"),
    DEMO("Demo Simulator"),
    ADB_TCP("ADB Stream (Port 8088)"),
    USB_SERIAL("Direct USB-C Serial")
}

sealed interface ConnectionStatus {
    data object Disconnected : ConnectionStatus
    data class Connecting(val message: String) : ConnectionStatus
    data class Connected(val mode: ConnectionMode, val detail: String) : ConnectionStatus
    data class Error(val message: String) : ConnectionStatus
}
