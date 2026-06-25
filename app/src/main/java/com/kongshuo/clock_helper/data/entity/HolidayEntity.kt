package com.kongshuo.clock_helper.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey val date: String,   // 日期，格式 "yyyy-MM-dd"
    val name: String,               // 节假日名称
    val isOffDay: Boolean,          // true=放假, false=调休上班
    val year: Int                   // 年份
)
