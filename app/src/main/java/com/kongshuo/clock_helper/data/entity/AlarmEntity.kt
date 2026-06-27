package com.kongshuo.clock_helper.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,                          // 小时 (0-23)
    val minute: Int,                        // 分钟 (0-59)
    val isEnabled: Boolean = true,          // 是否启用
    val isDailyReminder: Boolean = true,    // true=每日, false=按频率
    val reminderFrequencyMinutes: Int = 0,  // 频率提醒（分钟间隔），0=不使用
    val ringCount: Int = 5,                 // 响铃次数
    val alarmMusicUri: String = "",         // 铃声 URI
    val alarmMusicDisplayName: String = "", // 铃声显示名
    val isHolidaySkipped: Boolean = false,  // 是否跳过节假日
    val excludedDaysBitmask: Int = 0,       // 排除的星期位掩码 (Sun=1, Mon=2, ..., Sat=64)
    val label: String = "",                 // 标签
    val isVibrate: Boolean = true,          // 是否震动
    val lastFiredAt: Long? = null,          // 上次触发时间戳

    // 多免打扰时间段（JSON 字符串）
    val quietTimePeriodsJson: String = "",   // 多个免打扰时段，空串表示无

    // 以下旧字段保留仅用于数据库 v1→v2 迁移兼容，新代码不再使用
    @Deprecated("使用 quietTimePeriodsJson")
    val isQuietTimeEnabled: Boolean = false,
    @Deprecated("使用 quietTimePeriodsJson")
    val quietStartHour: Int = 0,
    @Deprecated("使用 quietTimePeriodsJson")
    val quietStartMinute: Int = 0,
    @Deprecated("使用 quietTimePeriodsJson")
    val quietEndHour: Int = 0,
    @Deprecated("使用 quietTimePeriodsJson")
    val quietEndMinute: Int = 0,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // 位掩码常量
        const val BIT_SUNDAY = 1     // 1 << 0
        const val BIT_MONDAY = 2     // 1 << 1
        const val BIT_TUESDAY = 4    // 1 << 2
        const val BIT_WEDNESDAY = 8  // 1 << 3
        const val BIT_THURSDAY = 16  // 1 << 4
        const val BIT_FRIDAY = 32   // 1 << 5
        const val BIT_SATURDAY = 64 // 1 << 6

        fun isDayExcluded(bitmask: Int, dayOfWeek: Int): Boolean {
            // dayOfWeek: Calendar.SUNDAY=1, MONDAY=2, ..., SATURDAY=7
            val bit = 1 shl (dayOfWeek - 1)
            return (bitmask and bit) != 0
        }

        fun setDayExcluded(bitmask: Int, dayOfWeek: Int, exclude: Boolean): Int {
            val bit = 1 shl (dayOfWeek - 1)
            return if (exclude) bitmask or bit else bitmask and bit.inv()
        }
    }
}
