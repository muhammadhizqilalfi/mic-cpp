package com.hizukiarufi.mic_cpp

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("mic_cpp")
        }
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    external fun initUdpSocket(): Int
    external fun sendUdpAudio(sock: Int, targetIp: String, targetPort: Int, audioData: ByteArray): Int

    private var socketFd: Int = -1
    private var audioRecord: AudioRecord? = null
    private var isStreaming = false
    private var streamingThread: Thread? = null

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val targetPort = 5000

    private lateinit var btnToggleStream: Button
    private lateinit var etServerIp: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etServerIp = findViewById(R.id.etServerIp)
        btnToggleStream = findViewById(R.id.btnToggleStream)

        // Inisialisasi socket UDP di awal
        socketFd = initUdpSocket()

        // Tombol interaktif untuk mulai/berhenti streaming
        btnToggleStream.setOnClickListener {
            if (!isStreaming) {
                checkPermissionAndStart()
            } else {
                stopAudioStream()
            }
        }
    }

    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        } else {
            startAudioStream()
        }
    }

    private fun startAudioStream() {
        if (socketFd < 0) {
            Log.e("MicApp", "Socket tidak valid")
            return
        }

        val serverIp = etServerIp.text.toString().trim()
        if (serverIp.isEmpty()) {
            Log.e("MicApp", "IP Server kosong")
            return
        }

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            minBufferSize * 2
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("MicApp", "AudioRecord gagal diinisialisasi")
            return
        }

        isStreaming = true
        audioRecord?.startRecording()
        btnToggleStream.text = "Berhentikan Streaming"
        btnToggleStream.setBackgroundColor(android.graphics.Color.RED)

        streamingThread = Thread {
            val buffer = ByteArray(minBufferSize)
            Log.d("MicApp", "Streaming dimulai ke $serverIp:$targetPort")

            while (isStreaming) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readSize > 0) {
                    sendUdpAudio(socketFd, serverIp, targetPort, buffer.copyOfRange(0, readSize))
                }
            }
        }
        streamingThread?.start()
    }

    private fun stopAudioStream() {
        isStreaming = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        streamingThread = null

        btnToggleStream.text = "Mulai Streaming"
        btnToggleStream.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
        Log.d("MicApp", "Streaming dihentikan")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startAudioStream()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudioStream()
    }
}