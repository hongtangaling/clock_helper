package com.kongshuo.clock_helper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kongshuo.clock_helper.data.dao.AlarmDao
import com.kongshuo.clock_helper.data.dao.HolidayDao
import com.kongshuo.clock_helper.data.entity.AlarmEntity
import com.kongshuo.clock_helper.data.entity.HolidayEntity

@Database(
    entities = [AlarmEntity::class, HolidayEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ClockDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun holidayDao(): HolidayDao

    companion object {
        const val DATABASE_NAME = "clock_helper.db"
    }
}
