package com.kongshuo.clock_helper.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kongshuo.clock_helper.data.repository.AlarmRepository
import com.kongshuo.clock_helper.domain.scheduler.AlarmScheduler
import com.kongshuo.clock_helper.util.ChineseHolidayProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * 每日凌晨重算闹钟调度
 * - 重新计算所有闹钟的下次触发时间
 * - 每周同步节假日数据
 */
@HiltWorker
class DailyRescheduleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    private val chineseHolidayProvider: ChineseHolidayProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 确保节假日数据已加载（当前年份 + 下一年）
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            chineseHolidayProvider.ensureHolidayData(currentYear, currentYear + 1)

            // 重新调度所有启用的闹钟
            val alarms = alarmRepository.getEnabledAlarmsOnce()
            alarmScheduler.rescheduleAll(alarms)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "daily_reschedule"

        /**
         * 安排每日重算任务
         */
        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyRescheduleWorker>(
                24, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
