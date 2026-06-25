package com.kongshuo.clock_helper.domain.calculator

import com.kongshuo.clock_helper.data.entity.AlarmEntity
import java.util.Calendar

/**
 * 核心算法：计算闹钟的下次触发时间
 *
 * 处理逻辑：
 * 1. 取当前时间 + 闹钟设定的时/分 → 计算今天的目标时间
 * 2. 若已过 → 根据 frequency（每日/间隔天数）计算下次日期
 * 3. 若启用节假日跳过 → 检查日期是否为法定节假日
 * 4. 若启用排除日 → 检查星期是否在排除列表中
 * 5. 若启用免打扰 → 判断是否落在免打扰时段，是则推迟到结束
 */
object NextFireTimeCalculator {

    data class Result(
        val fireTimeMillis: Long,
        val isDelayedByQuietTime: Boolean = false,
        val isSkippedByHoliday: Boolean = false,
        val isSkippedByExcludedDay: Boolean = false
    )

    /**
     * 计算闹钟的下次触发时间
     * @param alarm 闹钟实体
     * @param nowMillis 当前时间戳
     * @param isHolidayCheck 检查指定日期是否为节假日的函数 (dateStr: yyyy-MM-dd) -> Boolean
     * @return 计算结果，如果无法计算出有效时间则返回 null
     */
    fun calculate(
        alarm: AlarmEntity,
        nowMillis: Long = System.currentTimeMillis(),
        isHolidayCheck: (String) -> Boolean = { false }
    ): Result? {
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val target = Calendar.getInstance().apply { timeInMillis = nowMillis }

        // 设置目标时间为闹钟设定的时/分
        target.set(Calendar.HOUR_OF_DAY, alarm.hour)
        target.set(Calendar.MINUTE, alarm.minute)
        target.set(Calendar.SECOND, 0)
        target.set(Calendar.MILLISECOND, 0)

        // 如果今天的目标时间已经过了，推到下一个可用日期
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 根据提醒类型调整
        var maxIterations = 365 // 防止死循环
        while (maxIterations-- > 0) {
            var adjusted = false

            // 节假日跳过
            if (alarm.isHolidaySkipped) {
                val dateStr = dateToStr(target)
                if (isHolidayCheck(dateStr)) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                    adjusted = true
                }
            }

            // 排除日检查（工作日模式）
            if (!adjusted && alarm.excludedDaysBitmask != 0) {
                val dayOfWeek = target.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon, ..., 7=Sat
                if (AlarmEntity.isDayExcluded(alarm.excludedDaysBitmask, dayOfWeek)) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                    adjusted = true
                }
            }

            // 非每日提醒（按频率天数）
            if (!adjusted && !alarm.isDailyReminder && alarm.reminderFrequencyMinutes > 0) {
                // 对于频率模式，使用递进天数
                // 这里简化为：每次触发后，下次触发 = 当前 + frequency
                // 实际应用中应该跟踪 lastFiredAt
                if (alarm.lastFiredAt != null) {
                    val lastFire = Calendar.getInstance().apply { timeInMillis = alarm.lastFiredAt!! }
                    lastFire.add(Calendar.MINUTE, alarm.reminderFrequencyMinutes)
                    if (lastFire.after(target)) {
                        target.timeInMillis = lastFire.timeInMillis
                    }
                }
            }

            // 免打扰检查
            if (!adjusted && alarm.isQuietTimeEnabled) {
                val quietResult = checkQuietTime(
                    target, alarm.quietStartHour, alarm.quietStartMinute,
                    alarm.quietEndHour, alarm.quietEndMinute
                )
                if (quietResult != null) {
                    // 推迟到免打扰结束
                    target.timeInMillis = quietResult.timeInMillis
                    adjusted = true
                }
            }

            if (!adjusted) break
        }

        if (maxIterations <= 0) return null

        return Result(fireTimeMillis = target.timeInMillis)
    }

    /**
     * 检查目标时间是否在免打扰时段内
     * @return 免打扰结束的时间戳（Calendar），如果在时段外则返回 null
     */
    private fun checkQuietTime(
        target: Calendar,
        quietStartHour: Int,
        quietStartMinute: Int,
        quietEndHour: Int,
        quietEndMinute: Int
    ): Calendar? {
        val targetMinutes = target.get(Calendar.HOUR_OF_DAY) * 60 + target.get(Calendar.MINUTE)
        val startMinutes = quietStartHour * 60 + quietStartMinute
        val endMinutes = quietEndHour * 60 + quietEndMinute

        val isCrossDay = endMinutes <= startMinutes // 跨天免打扰

        val inQuietTime = if (isCrossDay) {
            // 跨天: 从 start 到午夜(1440) 或 从0到 end
            targetMinutes >= startMinutes || targetMinutes < endMinutes
        } else {
            // 同天: start <= target < end
            targetMinutes in startMinutes until endMinutes
        }

        if (!inQuietTime) return null

        // 计算免打扰结束时间
        val end = target.clone() as Calendar
        if (isCrossDay && targetMinutes < endMinutes) {
            // 已经在跨天的后半段（午夜后），结束时间在今天
            end.set(Calendar.HOUR_OF_DAY, quietEndHour)
            end.set(Calendar.MINUTE, quietEndMinute)
        } else {
            // 在跨天的前半段或同天，结束时间在明天
            end.add(Calendar.DAY_OF_YEAR, 1)
            end.set(Calendar.HOUR_OF_DAY, quietEndHour)
            end.set(Calendar.MINUTE, quietEndMinute)
        }
        end.set(Calendar.SECOND, 0)
        end.set(Calendar.MILLISECOND, 0)

        return end
    }

    private fun dateToStr(cal: Calendar): String {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(y, m, d)
    }

    /**
     * 检查免打扰是否覆盖全天
     */
    fun isFullDayQuietTime(
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int
    ): Boolean {
        val start = startHour * 60 + startMinute
        val end = endHour * 60 + endMinute
        return start == 0 && end == 1439
    }
}
