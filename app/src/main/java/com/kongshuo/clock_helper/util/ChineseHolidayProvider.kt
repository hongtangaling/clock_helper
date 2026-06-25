package com.kongshuo.clock_helper.util

import android.content.Context
import com.kongshuo.clock_helper.data.entity.HolidayEntity
import com.kongshuo.clock_helper.data.repository.HolidayRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 中国节假日数据提供器
 * 从内置 JSON 文件加载节假期数据，并可同步到数据库
 */
@Singleton
class ChineseHolidayProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val holidayRepository: HolidayRepository
) {
    /**
     * 从 assets 加载节假日数据到数据库
     */
    suspend fun loadHolidaysToDatabase(year: Int) {
        // 如果数据库中已有该年数据，跳过
        if (holidayRepository.countByYear(year) > 0) return

        try {
            val json = loadJsonFromAssets(year)
            val holidays = parseHolidayJson(json, year)
            holidayRepository.insertAll(holidays)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 确保指定年份的节假日数据已加载
     */
    suspend fun ensureHolidayData(vararg years: Int) {
        years.forEach { year ->
            if (holidayRepository.countByYear(year) == 0) {
                loadHolidaysToDatabase(year)
            }
        }
    }

    /**
     * 检查指定日期是否为节假日
     */
    suspend fun isHoliday(dateStr: String): Boolean {
        return holidayRepository.isOffDay(dateStr)
    }

    private fun loadJsonFromAssets(year: Int): String {
        return context.assets.open("chinese_holidays.json")
            .bufferedReader()
            .use { it.readText() }
    }

    private fun parseHolidayJson(json: String, year: Int): List<HolidayEntity> {
        val arr = JSONArray(json)
        val holidays = mutableListOf<HolidayEntity>()

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val date = obj.getString("date")
            val name = obj.getString("name")
            val isOffDay = obj.getBoolean("isOffDay")

            // 只加载指定年份
            if (date.startsWith("$year-")) {
                holidays.add(
                    HolidayEntity(
                        date = date,
                        name = name,
                        isOffDay = isOffDay,
                        year = year
                    )
                )
            }
        }

        return holidays
    }
}
