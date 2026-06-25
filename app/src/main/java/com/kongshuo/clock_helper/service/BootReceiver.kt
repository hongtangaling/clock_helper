package com.kongshuo.clock_helper.service

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
 * 开机广播接收器：设备重启后重新调度所有启用的闹钟
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var alarmRepository: AlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val alarms = alarmRepository.getEnabledAlarmsOnce()
                alarmScheduler.rescheduleAll(alarms)
            }
        }
    }
}
