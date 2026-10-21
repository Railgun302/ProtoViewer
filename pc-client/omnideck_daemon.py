#!/usr/bin/env python3
"""
OmniDeck Hardware Telemetry Daemon for Linux (Arch / CachyOS)
Streams Intel/AMD CPU Wattage, Nvidia/AMD GPU Wattage, NVMe temps, MangoHud FPS, and I/O to Moto Z over USB-C.
"""

import os
import sys
import time
import json
import socket
import glob
import subprocess
import argparse

try:
    import psutil
except ImportError:
    print("[!] 'psutil' no encontrado. Ejecuta en Arch/CachyOS: sudo pacman -S python-psutil")
    psutil = None

class HardwareMonitor:
    def __init__(self):
        self.last_time = time.time()
        self.last_rapl_energy = self.read_rapl_energy()
        self.last_net = self.read_net_bytes()
        self.last_disk = self.read_disk_bytes()
        self.gpu_type = self.detect_gpu()
        print(f"[+] GPU detectada: {self.gpu_type.upper()}")

    def detect_gpu(self):
        try:
            subprocess.run(['nvidia-smi'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
            return 'nvidia'
        except Exception:
            pass
        if glob.glob('/sys/class/drm/card*/device/hwmon'):
            return 'amd'
        return 'generic'

    def read_rapl_energy(self):
        paths = [
            '/sys/class/powercap/intel-rapl/intel-rapl:0/energy_uj',
            '/sys/class/powercap/intel-rapl/intel-rapl:0/intel-rapl:0:0/energy_uj'
        ]
        for p in paths:
            if os.path.exists(p):
                try:
                    with open(p, 'r') as f:
                        return int(f.read().strip())
                except Exception:
                    pass
        return None

    def read_cpu_wattage(self, delta_time):
        # 1. RAPL microjoules delta (Intel y AMD modernos en Linux 5.11+)
        curr_rapl = self.read_rapl_energy()
        if curr_rapl is not None and self.last_rapl_energy is not None and delta_time > 0:
            diff = curr_rapl - self.last_rapl_energy
            if diff < 0:
                diff += 2**32
            watts = (diff / 1000000.0) / delta_time
            self.last_rapl_energy = curr_rapl
            if 0 <= watts <= 500:
                return round(watts, 1)

        # 2. AMD hwmon power1_input o power1_average (zenpower / k10temp)
        for hwmon in glob.glob('/sys/class/hwmon/hwmon*'):
            name_p = os.path.join(hwmon, 'name')
            if os.path.exists(name_p):
                try:
                    with open(name_p, 'r') as nf:
                        name = nf.read().strip()
                    if name in ('k10temp', 'zenpower', 'coretemp'):
                        for p_file in ('power1_input', 'power1_average'):
                            full = os.path.join(hwmon, p_file)
                            if os.path.exists(full):
                                with open(full, 'r') as pf:
                                    return round(int(pf.read().strip()) / 1000000.0, 1)
                except Exception:
                    pass
        return 45.0

    def read_cpu_temp(self):
        for hwmon in glob.glob('/sys/class/hwmon/hwmon*'):
            name_p = os.path.join(hwmon, 'name')
            if os.path.exists(name_p):
                try:
                    with open(name_p, 'r') as nf:
                        name = nf.read().strip()
                    if name in ('coretemp', 'k10temp', 'zenpower', 'cpu_thermal'):
                        for t in ('temp1_input', 'temp2_input'):
                            tf = os.path.join(hwmon, t)
                            if os.path.exists(tf):
                                with open(tf, 'r') as f:
                                    return round(int(f.read().strip()) / 1000.0, 1)
                except Exception:
                    pass
        return 48.0

    def read_nvme(self):
        temp = 42.0
        for hwmon in glob.glob('/sys/class/hwmon/hwmon*'):
            name_p = os.path.join(hwmon, 'name')
            if os.path.exists(name_p):
                try:
                    with open(name_p, 'r') as nf:
                        name = nf.read().strip()
                    if 'nvme' in name:
                        tf = os.path.join(hwmon, 'temp1_input')
                        if os.path.exists(tf):
                            with open(tf, 'r') as f:
                                temp = round(int(f.read().strip()) / 1000.0, 1)
                                break
                except Exception:
                    pass
        return temp

    def read_gpu(self):
        gpu = {
            'name': 'GPU',
            'usage': 0.0,
            'temp_core': 45.0,
            'temp_hotspot': 52.0,
            'power_watts': 35.0,
            'vram_used_mb': 1024,
            'vram_total_mb': 8192,
            'fan_rpm': 0
        }
        if self.gpu_type == 'nvidia':
            try:
                cmd = [
                    'nvidia-smi',
                    '--query-gpu=name,utilization.gpu,temperature.gpu,power.draw,memory.used,memory.total,fan.speed',
                    '--format=csv,noheader,nounits'
                ]
                out = subprocess.check_output(cmd, encoding='utf-8').strip()
                parts = [p.strip() for p in out.split(',')]
                if len(parts) >= 7:
                    gpu['name'] = parts[0]
                    gpu['usage'] = float(parts[1])
                    gpu['temp_core'] = float(parts[2])
                    gpu['temp_hotspot'] = float(parts[2]) + 10.0
                    gpu['power_watts'] = float(parts[3])
                    gpu['vram_used_mb'] = int(float(parts[4]))
                    gpu['vram_total_mb'] = int(float(parts[5]))
                    gpu['fan_rpm'] = int(float(parts[6]) * 25.0)
                    return gpu
            except Exception:
                pass

        elif self.gpu_type == 'amd':
            try:
                for hwmon in glob.glob('/sys/class/drm/card*/device/hwmon/hwmon*'):
                    p_file = os.path.join(hwmon, 'power1_average')
                    if not os.path.exists(p_file):
                        p_file = os.path.join(hwmon, 'power1_input')
                    if os.path.exists(p_file):
                        with open(p_file, 'r') as f:
                            gpu['power_watts'] = round(int(f.read().strip()) / 1000000.0, 1)

                    t_core = os.path.join(hwmon, 'temp1_input')
                    t_hot = os.path.join(hwmon, 'temp2_input')
                    if os.path.exists(t_core):
                        with open(t_core, 'r') as f:
                            gpu['temp_core'] = round(int(f.read().strip()) / 1000.0, 1)
                    if os.path.exists(t_hot):
                        with open(t_hot, 'r') as f:
                            gpu['temp_hotspot'] = round(int(f.read().strip()) / 1000.0, 1)

                    fan = os.path.join(hwmon, 'fan1_input')
                    if os.path.exists(fan):
                        with open(fan, 'r') as f:
                            gpu['fan_rpm'] = int(f.read().strip())

                for bus in glob.glob('/sys/class/drm/card*/device/gpu_busy_percent'):
                    with open(bus, 'r') as f:
                        gpu['usage'] = float(f.read().strip())
                gpu['name'] = 'AMD Radeon GPU'
                return gpu
            except Exception:
                pass

        return gpu

    def read_mangohud(self):
        res = {'active': False, 'fps': 0.0, 'fps_1percent': 0.0, 'frametime_ms': 0.0, 'app_name': ''}
        shm_file = '/dev/shm/mangohud'
        if os.path.exists(shm_file):
            try:
                with open(shm_file, 'r') as f:
                    data = json.load(f)
                    res['active'] = True
                    res['fps'] = float(data.get('fps', 0.0))
                    res['fps_1percent'] = float(data.get('fps_1percent', res['fps'] * 0.85))
                    res['frametime_ms'] = float(data.get('frametime', 1000.0 / max(1.0, res['fps'])))
                    res['app_name'] = str(data.get('engine', 'Game'))
            except Exception:
                pass
        return res

    def read_net_bytes(self):
        if psutil:
            net = psutil.net_io_counters()
            return (net.bytes_recv, net.bytes_sent)
        return (0, 0)

    def read_disk_bytes(self):
        if psutil:
            d = psutil.disk_io_counters()
            if d:
                return (d.read_bytes, d.write_bytes)
        return (0, 0)

    def capture_snapshot(self):
        now = time.time()
        dt = max(0.01, now - self.last_time)
        self.last_time = now

        cpu_usage = psutil.cpu_percent() if psutil else 25.0
        cores = psutil.cpu_percent(percpu=True) if psutil else [cpu_usage]
        freq = int(psutil.cpu_freq().current) if (psutil and psutil.cpu_freq()) else 4200
        cpu_temp = self.read_cpu_temp()
        cpu_power = self.read_cpu_wattage(dt)

        gpu = self.read_gpu()
        gpu_power = gpu['power_watts']
        total_power = round(cpu_power + gpu_power + 25.0, 1)

        nvme_temp = self.read_nvme()
        disk_now = self.read_disk_bytes()
        disk_read_mbps = round(max(0, disk_now[0] - self.last_disk[0]) / (1024 * 1024 * dt), 1)
        disk_write_mbps = round(max(0, disk_now[1] - self.last_disk[1]) / (1024 * 1024 * dt), 1)
        self.last_disk = disk_now

        net_now = self.read_net_bytes()
        down_mbps = round(max(0, net_now[0] - self.last_net[0]) * 8 / (1000000 * dt), 1)
        up_mbps = round(max(0, net_now[1] - self.last_net[1]) * 8 / (1000000 * dt), 1)
        self.last_net = net_now

        ram_used = int(psutil.virtual_memory().used / (1024*1024)) if psutil else 8192
        ram_total = int(psutil.virtual_memory().total / (1024*1024)) if psutil else 32768

        gaming = self.read_mangohud()

        return {
            'timestamp': int(now * 1000),
            'cpu': {
                'usage': cpu_usage,
                'temp': cpu_temp,
                'power_watts': cpu_power,
                'freq_mhz': freq,
                'cores': cores[:16]
            },
            'gpu': gpu,
            'power': {
                'total_watts': total_power,
                'cpu_watts': cpu_power,
                'gpu_watts': gpu_power
            },
            'nvme': {
                'name': 'NVMe SSD',
                'temp_c': nvme_temp,
                'disk_read_mbps': disk_read_mbps,
                'disk_write_mbps': disk_write_mbps
            },
            'ram': {
                'used_mb': ram_used,
                'total_mb': ram_total
            },
            'gaming': gaming,
            'net': {
                'down_mbps': down_mbps,
                'up_mbps': up_mbps
            }
        }

def run_tcp_server(port=8088, interval=1.0):
    monitor = HardwareMonitor()
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind(('0.0.0.0', port))
    s.listen(1)
    print(f"\n=======================================================")
    print(f"  [OmniDeck Daemon] Servidor listo en 0.0.0.0:{port}")
    print(f"  Tasa de actualización: {interval} seg (Bajo consumo de CPU y ADB)")
    print(f"  Paso siguiente: Ejecuta en otra terminal:")
    print(f"      adb reverse tcp:8088 tcp:8088")
    print(f"=======================================================\n")
    print("[*] Esperando que la app OmniDeck en tu Moto Z se conecte...")

    while True:
        try:
            conn, addr = s.accept()
            print(f"[✓] Moto Z conectado desde: {addr} (Transmitiendo telemetría en tiempo real cada {interval}s)")
            while True:
                payload = monitor.capture_snapshot()
                line = json.dumps(payload) + "\n"
                conn.sendall(line.encode('utf-8'))
                time.sleep(interval)
        except (BrokenPipeError, ConnectionResetError):
            print("[-] Moto Z desconectado. Esperando reconexión...")
        except KeyboardInterrupt:
            print("\n[!] Deteniendo OmniDeck Daemon.")
            break
        except Exception as e:
            print(f"[!] Error: {e}")
            time.sleep(1)

def run_serial_stream(port_name='/dev/ttyACM0', baud=115200, interval=1.0):
    import serial
    monitor = HardwareMonitor()
    print(f"[+] Abriendo puerto serie USB en {port_name} a {baud} baudios ({interval}s intervalo)...")
    ser = serial.Serial(port_name, baud, timeout=1)
    while True:
        try:
            payload = monitor.capture_snapshot()
            line = json.dumps(payload) + "\n"
            ser.write(line.encode('utf-8'))
            time.sleep(interval)
        except KeyboardInterrupt:
            ser.close()
            break

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="OmniDeck Telemetry Server para Arch Linux / CachyOS")
    parser.add_argument('--tcp', type=int, default=8088, help="Puerto TCP para streaming ADB (default: 8088)")
    parser.add_argument('--serial', type=str, default=None, help="Dispositivo serie USB (ej. /dev/ttyACM0)")
    parser.add_argument('--rate', type=float, default=1.0, help="Intervalo en segundos (1.0 = 1s recomendado, 5.0 = 5s ultra-ahorro)")
    args = parser.parse_args()

    if args.serial:
        run_serial_stream(args.serial, 115200, args.rate)
    else:
        run_tcp_server(args.tcp, args.rate)
