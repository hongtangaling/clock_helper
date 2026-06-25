package com.kongshuo.clock_helper.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 闹钟响铃播放器封装
 * 支持渐升音量、震动、音频焦点管理
 */
@Singleton
class AlarmPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var isPlaying = false
    private var isVibrating = false

    // 渐升音量参数
    private val maxVolume = 1.0f
    private val volumeRampDuration = 5000L // 5秒渐升到最大
    private var rampStartTime = 0L

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * 开始播放闹钟铃声
     * @param uri 铃声 URI，空则使用默认闹钟铃声
     * @param vibrate 是否震动
     */
    fun start(uri: String? = null, vibrate: Boolean = true) {
        if (isPlaying) return

        // 请求音频焦点
        if (!requestAudioFocus()) return

        try {
            val alarmUri = if (!uri.isNullOrEmpty()) {
                Uri.parse(uri)
            } else {
                android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alarmUri)
                isLooping = true
                setVolume(0.1f, 0.1f) // 从 10% 音量开始
                prepare()
                start()
            }

            rampStartTime = System.currentTimeMillis()
            isPlaying = true

            // 开始渐升音量
            startVolumeRamp()

            // 开始震动
            if (vibrate) {
                startVibration()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 停止播放
     */
    fun stop() {
        isPlaying = false
        isVibrating = false

        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        vibrator?.cancel()
        abandonAudioFocus()
    }

    fun isPlaying(): Boolean = isPlaying

    private fun startVolumeRamp() {
        Thread {
            while (isPlaying) {
                val elapsed = System.currentTimeMillis() - rampStartTime
                val volume = minOf(maxVolume, 0.1f + (elapsed.toFloat() / volumeRampDuration) * 0.9f)
                mediaPlayer?.setVolume(volume, volume)
                Thread.sleep(200)
            }
        }.apply { isDaemon = true }.start()
    }

    private fun startVibration() {
        isVibrating = true
        // 震动模式：响 1 秒，停 0.5 秒
        val pattern = longArrayOf(0, 1000, 500)
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 0) // 重复
                it.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(pattern, 0)
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()
            audioManager?.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) ==
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }
}
