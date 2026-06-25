package com.kongshuo.clock_helper.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kongshuo.clock_helper.data.entity.HolidayEntity

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays WHERE year = :year ORDER BY date ASC")
    suspend fun getHolidaysByYear(year: Int): List<HolidayEntity>

    @Query("SELECT * FROM holidays WHERE date = :date LIMIT 1")
    suspend fun getHolidayByDate(date: String): HolidayEntity?

    @Query("SELECT COUNT(*) FROM holidays WHERE date = :date AND isOffDay = 1")
    suspend fun isOffDay(date: String): Int

    @Query("SELECT COUNT(*) FROM holidays WHERE date = :date AND isOffDay = 0")
    suspend fun isWorkDay(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(holidays: List<HolidayEntity>)

    @Query("DELETE FROM holidays WHERE year = :year")
    suspend fun deleteByYear(year: Int)

    @Query("SELECT COUNT(*) FROM holidays WHERE year = :year")
    suspend fun countByYear(year: Int): Int
}
