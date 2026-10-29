package com.example.nfc

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Authentic electromagnetic lock sound: "trrr-beep!"
     * Rapid clicking solenoid sound followed by high-pitched access confirmation beep.
     */
    fun playIntercomUnlockSound() {
        scope.launch {
            vibratePattern(longArrayOf(0, 30, 40, 30, 40, 120), intArrayOf(0, 180, 0, 200, 0, 255))
            generatePcmAudio {
                val sampleRate = 44100
                val totalSamples = (sampleRate * 0.45).toInt()
                val buffer = ShortArray(totalSamples)

                // 1. Rapid solenoid buzz ("trrr") for first 0.25s
                val buzzSamples = (sampleRate * 0.25).toInt()
                for (i in 0 until buzzSamples) {
                    val t = i.toDouble() / sampleRate
                    // 50Hz electromagnetic buzz with harmonics
                    val buzz = sin(2.0 * Math.PI * 50.0 * t) * 0.4 +
                            sin(2.0 * Math.PI * 150.0 * t) * 0.3 +
                            sin(2.0 * Math.PI * 250.0 * t) * 0.2
                    // Pulse envelope to create clicks
                    val pulse = if ((i % (sampleRate / 30)) < (sampleRate / 80)) 1.0 else 0.1
                    buffer[i] = (buzz * pulse * Short.MAX_VALUE * 0.7).toInt().toShort()
                }

                // 2. High-pitch confirmation chime ("beep!") for remaining 0.20s at 1800Hz
                for (i in buzzSamples until totalSamples) {
                    val t = (i - buzzSamples).toDouble() / sampleRate
                    val chime = sin(2.0 * Math.PI * 1850.0 * t)
                    val envelope = 1.0 - (t / 0.20) // Fade out
                    buffer[i] = (chime * envelope * Short.MAX_VALUE * 0.6).toInt().toShort()
                }
                buffer
            }
        }
    }

    /**
     * Authentic Metro transit gate ding sound (two harmonic bells)
     */
    fun playMetroDing() {
        scope.launch {
            vibrateSuccess()
            generatePcmAudio {
                val sampleRate = 44100
                val totalSamples = (sampleRate * 0.35).toInt()
                val buffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = if (t < 0.15) 1200.0 else 1600.0
                    val decay = if (t < 0.15) 1.0 - (t / 0.15) * 0.3 else 1.0 - ((t - 0.15) / 0.2)
                    val sample = sin(2.0 * Math.PI * freq * t) * decay
                    buffer[i] = (sample * Short.MAX_VALUE * 0.5).toInt().toShort()
                }
                buffer
            }
        }
    }

    /**
     * POS Terminal contactless approval double beep (Apple Pay / EMV tone)
     */
    fun playPosBeep() {
        scope.launch {
            vibrateSuccess()
            generatePcmAudio {
                val sampleRate = 44100
                val totalSamples = (sampleRate * 0.28).toInt()
                val buffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    // Dual tone chime: 2400Hz
                    val sample = sin(2.0 * Math.PI * 2400.0 * t) * (1.0 - t / 0.28)
                    buffer[i] = (sample * Short.MAX_VALUE * 0.55).toInt().toShort()
                }
                buffer
            }
        }
    }

    private fun generatePcmAudio(pcmBuilder: () -> ShortArray) {
        try {
            val audioData = pcmBuilder()
            val sampleRate = 44100
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(audioData.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(audioData, 0, audioData.size)
            track.play()
        } catch (_: Exception) {}
    }

    fun vibrateSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(timings: LongArray, amplitudes: IntArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }
}
