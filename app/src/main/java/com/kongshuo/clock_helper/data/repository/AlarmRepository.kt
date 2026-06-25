package com.kongshuo.clock_helper.data.repository

import com.kongshuo.clock_helper.data.dao.AlarmDao
import com.kongshuo.clock_helper.data.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) {
    fun getAllAlarms(): Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

    fun getEnabledAlarms(): Flow<List<AlarmEntity>> = alarmDao.getEnabledAlarms()

    suspend fun getEnabledAlarmsOnce(): List<AlarmEntity> = alarmDao.getEnabledAlarmsOnce()

    suspend fun getAlarmById(id: Long): AlarmEntity? = alarmDao.getAlarmById(id)

    fun getAlarmByIdFlow(id: Long): Flow<AlarmEntity?> = alarmDao.getAlarmByIdFlow(id)

    suspend fun insertAlarm(alarm: AlarmEntity): Long = alarmDao.insertAlarm(alarm)

    suspend fun updateAlarm(alarm: AlarmEntity) = alarmDao.updateAlarm(alarm)

    suspend fun deleteAlarm(alarm: AlarmEntity) = alarmDao.deleteAlarm(alarm)

    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)

    suspend fun setAlarmEnabled(id: Long, isEnabled: Boolean) {
        alarmDao.setAlarmEnabled(id, isEnabled)
    }
}
