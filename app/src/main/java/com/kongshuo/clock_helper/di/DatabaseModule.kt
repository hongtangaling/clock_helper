package com.kongshuo.clock_helper.di

import android.content.Context
import androidx.room.Room
import com.kongshuo.clock_helper.data.ClockDatabase
import com.kongshuo.clock_helper.data.dao.AlarmDao
import com.kongshuo.clock_helper.data.dao.HolidayDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ClockDatabase {
        return Room.databaseBuilder(
            context,
            ClockDatabase::class.java,
            ClockDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideAlarmDao(database: ClockDatabase): AlarmDao {
        return database.alarmDao()
    }

    @Provides
    fun provideHolidayDao(database: ClockDatabase): HolidayDao {
        return database.holidayDao()
    }
}
