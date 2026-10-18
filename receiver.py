import socket
import numpy as np
import sounddevice as sd

UDP_IP = "0.0.0.0"
UDP_PORT = 5000

# Inisialisasi UDP Socket
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind((UDP_IP, UDP_PORT))

# Konfigurasi Audio Output (harus sama dengan di Android: 44100Hz, Mono, 16-bit)
samplerate = 44100
channels = 1

print(f"Mendengarkan streaming audio di port {UDP_PORT}... Silakan ngomong di HP!")

# Membuka jalur pemutaran audio real-time di PC
stream = sd.OutputStream(samplerate=samplerate, channels=channels, dtype='int16')
stream.start()

try:
    while True:
        data, addr = sock.recvfrom(4096)

        # Mengubah byte mentah PCM dari HP menjadi array angka numpy
        audio_data = np.frombuffer(data, dtype=np.int16)

        # Memutar langsung ke speaker PC
        stream.write(audio_data)

except KeyboardInterrupt:
    print("\nStreaming dihentikan.")
finally:
    stream.stop()
    stream.close()
    sock.close()