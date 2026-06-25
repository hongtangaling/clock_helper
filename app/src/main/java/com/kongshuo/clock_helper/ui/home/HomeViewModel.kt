package com.kongshuo.clock_helper.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kongshuo.clock_helper.data.entity.AlarmEntity
import com.kongshuo.clock_helper.data.repository.AlarmRepository
import com.kongshuo.clock_helper.domain.calculator.NextFireTimeCalculator
import com.kongshuo.clock_helper.domain.scheduler.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val alarms: List<AlarmEntity> = emptyList(),
    val nextAlarm: AlarmEntity? = null,
    val nextAlarmTime: Long? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val alarms: StateFlow<List<AlarmEntity>> = alarmRepository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _nextAlarm = MutableStateFlow<AlarmEntity?>(null)
    val nextAlarm: StateFlow<AlarmEntity?> = _nextAlarm.asStateFlow()

    private val _nextAlarmTime = MutableStateFlow<Long?>(null)
    val nextAlarmTime: StateFlow<Long?> = _nextAlarmTime.asStateFlow()

    init {
        viewModelScope.launch {
            alarms.collect { list ->
                computeNextAlarm(list)
            }
        }
    }

    private suspend fun computeNextAlarm(alarms: List<AlarmEntity>) {
        val enabledAlarms = alarms.filter { it.isEnabled }
        if (enabledAlarms.isEmpty()) {
            _nextAlarm.value = null
            _nextAlarmTime.value = null
            return
        }

        var nearest: AlarmEntity? = null
        var nearestTime = Long.MAX_VALUE

        for (alarm in enabledAlarms) {
            val result = NextFireTimeCalculator.calculate(alarm)
            if (result != null && result.fireTimeMillis in 0 until nearestTime) {
                nearest = alarm
                nearestTime = result.fireTimeMillis
            }
        }

        _nextAlarm.value = nearest
        _nextAlarmTime.value = if (nearestTime != Long.MAX_VALUE) nearestTime else null
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val newEnabled = !alarm.isEnabled
            alarmRepository.setAlarmEnabled(alarm.id, newEnabled)
            val updated = alarm.copy(isEnabled = newEnabled)
            if (newEnabled) {
                val result = NextFireTimeCalculator.calculate(updated)
                if (result != null) {
                    alarmScheduler.schedule(updated, result.fireTimeMillis)
                }
            } else {
                alarmScheduler.cancel(updated)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmRepository.deleteAlarm(alarm)
            alarmScheduler.cancel(alarm)
        }
    }
}
