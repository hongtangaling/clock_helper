package com.kongshuo.clock_helper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kongshuo.clock_helper.service.AlarmService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ClockApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 闹钟响铃通道
            val alarmChannel = NotificationChannel(
                AlarmService.CHANNEL_ID,
                "闹钟响铃",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "闹钟响铃时的前台服务通知"
                enableVibration(true)
                setSound(null, null)
            }

            // 普通通知通道
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "一般通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "其他应用通知"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(alarmChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }

    companion object {
        const val CHANNEL_GENERAL = "general"
    }
}
