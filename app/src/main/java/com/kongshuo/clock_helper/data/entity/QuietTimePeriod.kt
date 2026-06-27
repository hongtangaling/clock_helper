package com.kongshuo.clock_helper.data.entity

import org.json.JSONArray
import org.json.JSONObject

/**
 * 免打扰时段
 */
data class QuietTimePeriod(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
) {
    companion object {
        fun toJson(periods: List<QuietTimePeriod>): String {
            val arr = JSONArray()
            for (p in periods) {
                arr.put(JSONObject().apply {
                    put("startHour", p.startHour)
                    put("startMinute", p.startMinute)
                    put("endHour", p.endHour)
                    put("endMinute", p.endMinute)
                })
            }
            return arr.toString()
        }

        fun fromJson(json: String): List<QuietTimePeriod> {
            if (json.isBlank()) return emptyList()
            return try {
                val arr = JSONArray(json)
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    QuietTimePeriod(
                        startHour = obj.getInt("startHour"),
                        startMinute = obj.getInt("startMinute"),
                        endHour = obj.getInt("endHour"),
                        endMinute = obj.getInt("endMinute")
                    )
                }
            } catch (_: Exception) {
                emptyList()
            }
        }

        /** 将 v1 旧字段转换为多时段 JSON */
        fun fromLegacy(
            enabled: Boolean,
            startHour: Int, startMinute: Int,
            endHour: Int, endMinute: Int
        ): String {
            if (!enabled) return ""
            return toJson(listOf(QuietTimePeriod(startHour, startMinute, endHour, endMinute)))
        }
    }
}
