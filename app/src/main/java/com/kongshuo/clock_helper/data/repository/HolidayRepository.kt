package com.kongshuo.clock_helper.data.repository

import com.kongshuo.clock_helper.data.dao.HolidayDao
import com.kongshuo.clock_helper.data.entity.HolidayEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayRepository @Inject constructor(
    private val holidayDao: HolidayDao
) {
    suspend fun getHolidaysByYear(year: Int): List<HolidayEntity> =
        holidayDao.getHolidaysByYear(year)

    suspend fun getHolidayByDate(date: String): HolidayEntity? =
        holidayDao.getHolidayByDate(date)

    /**
     * 检查指定日期是否为法定节假日（放假）
     */
    suspend fun isOffDay(date: String): Boolean =
        holidayDao.isOffDay(date) > 0

    /**
     * 检查指定日期是否为调休上班日
     */
    suspend fun isWorkDay(date: String): Boolean =
        holidayDao.isWorkDay(date) > 0

    suspend fun insertAll(holidays: List<HolidayEntity>) =
        holidayDao.insertAll(holidays)

    suspend fun deleteByYear(year: Int) =
        holidayDao.deleteByYear(year)

    suspend fun countByYear(year: Int): Int =
        holidayDao.countByYear(year)
}
