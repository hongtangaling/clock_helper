package com.kongshuo.clock_helper.domain.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kongshuo.clock_helper.data.entity.AlarmEntity
import com.kongshuo.clock_helper.service.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * 调度闹钟
     */
    fun schedule(alarm: AlarmEntity, fireTimeMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_HOUR, alarm.hour)
            putExtra(EXTRA_ALARM_MINUTE, alarm.minute)
            putExtra(EXTRA_RING_COUNT, alarm.ringCount)
            putExtra(EXTRA_MUSIC_URI, alarm.alarmMusicUri)
            putExtra(EXTRA_IS_VIBRATE, alarm.isVibrate)
            putExtra(EXTRA_LABEL, alarm.label)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 使用精确闹钟（需要 USE_EXACT_ALARM 权限）
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            fireTimeMillis,
            pendingIntent
        )
    }

    /**
     * 取消闹钟调度
     */
    fun cancel(alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * 重新调度所有启用的闹钟
     */
    suspend fun rescheduleAll(alarms: List<AlarmEntity>) {
        // 先取消所有
        alarms.forEach { cancel(it) }

        // 重新调度
        val calculator = com.kongshuo.clock_helper.domain.calculator.NextFireTimeCalculator
        alarms.forEach { alarm ->
            val result = calculator.calculate(alarm)
            if (result != null) {
                schedule(alarm, result.fireTimeMillis)
            }
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
        const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"
        const val EXTRA_RING_COUNT = "extra_ring_count"
        const val EXTRA_MUSIC_URI = "extra_music_uri"
        const val EXTRA_IS_VIBRATE = "extra_is_vibrate"
        const val EXTRA_LABEL = "extra_label"
    }
}
