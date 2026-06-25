package com.kongshuo.clock_helper.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kongshuo.clock_helper.MainActivity
import com.kongshuo.clock_helper.R
import com.kongshuo.clock_helper.domain.scheduler.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 前台服务：闹钟响铃时持续播放，防止被系统杀死
 */
@AndroidEntryPoint
class AlarmService : Service() {

    @Inject lateinit var alarmPlayer: AlarmPlayer

    private var ringCount = 0
    private var maxRingCount = 5
    private var ringStartTime = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
        val musicUri = intent?.getStringExtra(AlarmScheduler.EXTRA_MUSIC_URI) ?: ""
        val isVibrate = intent?.getBooleanExtra(AlarmScheduler.EXTRA_IS_VIBRATE, true) ?: true
        maxRingCount = intent?.getIntExtra(AlarmScheduler.EXTRA_RING_COUNT, 5) ?: 5
        val label = intent?.getStringExtra(AlarmScheduler.EXTRA_LABEL) ?: ""
        val hour = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_HOUR, 0) ?: 0
        val minute = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_MINUTE, 0) ?: 0

        ringCount = 0
        ringStartTime = System.currentTimeMillis()

        // 启动前台服务
        val notification = createNotification(alarmId, label, hour, minute)
        startForeground(NOTIFICATION_ID, notification)

        // 开始播放铃声
        alarmPlayer.start(musicUri, isVibrate)

        // 开始计数线程
        startRingCounter()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        alarmPlayer.stop()
        super.onDestroy()
    }

    /**
     * 停止闹钟
     */
    fun stopAlarm() {
        alarmPlayer.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * 贪睡（延后 5 分钟）
     */
    fun snooze() {
        alarmPlayer.stop()
        // TODO: 实现贪睡逻辑 - 5分钟后重新触发
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startRingCounter() {
        Thread {
            while (ringCount < maxRingCount) {
                try {
                    Thread.sleep(60_000L) // 每分钟计数一次
                    ringCount++
                } catch (e: InterruptedException) {
                    break
                }
            }
            // 响铃次数用尽，停止
            if (ringCount >= maxRingCount) {
                stopAlarm()
            }
        }.apply { isDaemon = true }.start()
    }

    private fun createNotification(
        alarmId: Long,
        label: String,
        hour: Int,
        minute: Int
    ): Notification {
        val timeStr = "%02d:%02d".format(hour, minute)
        val title = if (label.isNotEmpty()) label else "闹钟"
        val text = "$timeStr - 点击关闭"

        val stopIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_STOP_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId.toInt(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val activityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(activityPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "关闭", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(activityPendingIntent, true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "闹钟响铃",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "闹钟响铃时的通知"
            enableVibration(true)
            setSound(null, null) // 不播放通知声音（我们自己播放）
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "alarm_ringing"
        const val NOTIFICATION_ID = 1001
    }
}
