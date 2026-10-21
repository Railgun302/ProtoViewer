#!/usr/bin/env bash
# Script de inicio rápido para Arch Linux / CachyOS
set -e

echo "[OmniDeck] Verificando dependencias en Arch Linux / CachyOS..."
if ! command -v python3 &> /dev/null; then
    sudo pacman -S --needed python
fi

# Instalar dependencias si no existen
python3 -c "import psutil" 2>/dev/null || {
    echo "[OmniDeck] Instalando python-psutil..."
    sudo pacman -S --needed python-psutil lm_sensors
}

# Redirigir puerto ADB si hay dispositivo conectado
if command -v adb &> /dev/null; then
    echo "[OmniDeck] Configurando túnel USB con 'adb reverse tcp:8088 tcp:8088'..."
    adb reverse tcp:8088 tcp:8088 || echo "[!] Advertencia: Dispositivo no detectado en ADB aún. Asegúrate de conectar el Moto Z por USB."
fi

RATE="${1:-1.0}"
# Iniciar daemon
echo "[OmniDeck] Iniciando servidor de telemetría a ${RATE}s intervalo..."
python3 "$(dirname "$0")/omnideck_daemon.py" --tcp 8088 --rate "$RATE"
