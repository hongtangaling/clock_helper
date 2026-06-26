package com.kongshuo.clock_helper.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kongshuo.clock_helper.data.repository.AlarmRepository
import com.kongshuo.clock_helper.domain.scheduler.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 接收 AlarmManager 触发的广播，启动前台服务播放闹钟
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var alarmRepository: AlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STOP_ALARM -> {
                handleStopAlarm(context, intent)
            }
            ACTION_SNOOZE -> {
                handleSnooze(context)
            }
            else -> {
                handleAlarmTrigger(context, intent)
            }
        }
    }

    private fun handleAlarmTrigger(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        if (alarmId == -1L) return

        // 启动前台服务播放闹钟
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtras(intent.extras ?: return)
        }
        context.startForegroundService(serviceIntent)

        // 更新闹钟最后触发时间
        CoroutineScope(Dispatchers.IO).launch {
            val alarm = alarmRepository.getAlarmById(alarmId) ?: return@launch
            val updatedAlarm = alarm.copy(lastFiredAt = System.currentTimeMillis())
            alarmRepository.updateAlarm(updatedAlarm)

            // 重新调度下次触发（使用更新后的 alarm，lastFiredAt 对频率模式计算至关重要）
            val calculator = com.kongshuo.clock_helper.domain.calculator.NextFireTimeCalculator
            val result = calculator.calculate(updatedAlarm)
            if (result != null) {
                alarmScheduler.schedule(updatedAlarm, result.fireTimeMillis)
            }
        }
    }

    private fun handleStopAlarm(context: Context, intent: Intent) {
        val stopIntent = Intent(context, AlarmService::class.java)
        context.stopService(stopIntent)

        // 取消通知
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(AlarmService.NOTIFICATION_ID)
    }

    private fun handleSnooze(context: Context) {
        val stopIntent = Intent(context, AlarmService::class.java)
        context.stopService(stopIntent)
    }

    companion object {
        const val ACTION_STOP_ALARM = "com.kongshuo.clock_helper.action.STOP_ALARM"
        const val ACTION_SNOOZE = "com.kongshuo.clock_helper.action.SNOOZE"
    }
}
