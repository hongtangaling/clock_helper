package com.kongshuo.clock_helper.ui.editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kongshuo.clock_helper.data.entity.AlarmEntity
import com.kongshuo.clock_helper.data.repository.AlarmRepository
import com.kongshuo.clock_helper.permission.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorUiState(
    val isEditMode: Boolean = false,
    val hour: Int = 8,
    val minute: Int = 0,
    val label: String = "",
    val isDailyReminder: Boolean = true,
    val reminderFrequencyMinutes: Int = 0,
    val ringCount: Int = 5,
    val alarmMusicUri: String = "",
    val alarmMusicDisplayName: String = "",
    val isHolidaySkipped: Boolean = false,
    val excludedDaysBitmask: Int = 0,
    val isVibrate: Boolean = true,
    val isQuietTimeEnabled: Boolean = false,
    val quietStartHour: Int = 22,
    val quietStartMinute: Int = 0,
    val quietEndHour: Int = 8,
    val quietEndMinute: Int = 0,
    val isEnabled: Boolean = true,
    val canSave: Boolean = true
)

@HiltViewModel
class AlarmEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alarmRepository: AlarmRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val alarmId: Long? = savedStateHandle.get<Long>("alarmId")

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        if (alarmId != null && alarmId > 0) {
            loadAlarm(alarmId)
        }
        checkPermissions()
    }

    private fun loadAlarm(id: Long) {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(id) ?: return@launch
            _uiState.value = _uiState.value.copy(
                isEditMode = true,
                hour = alarm.hour,
                minute = alarm.minute,
                label = alarm.label,
                isDailyReminder = alarm.isDailyReminder,
                reminderFrequencyMinutes = alarm.reminderFrequencyMinutes,
                ringCount = alarm.ringCount,
                alarmMusicUri = alarm.alarmMusicUri,
                alarmMusicDisplayName = alarm.alarmMusicDisplayName,
                isHolidaySkipped = alarm.isHolidaySkipped,
                excludedDaysBitmask = alarm.excludedDaysBitmask,
                isVibrate = alarm.isVibrate,
                isQuietTimeEnabled = alarm.isQuietTimeEnabled,
                quietStartHour = alarm.quietStartHour,
                quietStartMinute = alarm.quietStartMinute,
                quietEndHour = alarm.quietEndHour,
                quietEndMinute = alarm.quietEndMinute,
                isEnabled = alarm.isEnabled
            )
        }
    }

    private fun checkPermissions() {
        _uiState.value = _uiState.value.copy(
            canSave = permissionManager.canSaveAlarm()
        )
    }

    fun updateHour(hour: Int) { _uiState.value = _uiState.value.copy(hour = hour) }
    fun updateMinute(minute: Int) { _uiState.value = _uiState.value.copy(minute = minute) }
    fun updateLabel(label: String) { _uiState.value = _uiState.value.copy(label = label) }
    fun updateIsDailyReminder(isDaily: Boolean) { _uiState.value = _uiState.value.copy(isDailyReminder = isDaily) }
    fun updateFrequency(minutes: Int) { _uiState.value = _uiState.value.copy(reminderFrequencyMinutes = minutes) }
    fun updateRingCount(count: Int) { _uiState.value = _uiState.value.copy(ringCount = count) }
    fun updateMusic(uri: String, displayName: String) {
        _uiState.value = _uiState.value.copy(alarmMusicUri = uri, alarmMusicDisplayName = displayName)
    }
    fun updateHolidaySkip(skip: Boolean) { _uiState.value = _uiState.value.copy(isHolidaySkipped = skip) }
    fun updateExcludedDays(bitmask: Int) { _uiState.value = _uiState.value.copy(excludedDaysBitmask = bitmask) }
    fun updateVibrate(vibrate: Boolean) { _uiState.value = _uiState.value.copy(isVibrate = vibrate) }
    fun updateQuietTimeEnabled(enabled: Boolean) { _uiState.value = _uiState.value.copy(isQuietTimeEnabled = enabled) }
    fun updateQuietStart(hour: Int, minute: Int) { _uiState.value = _uiState.value.copy(quietStartHour = hour, quietStartMinute = minute) }
    fun updateQuietEnd(hour: Int, minute: Int) { _uiState.value = _uiState.value.copy(quietEndHour = hour, quietEndMinute = minute) }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val entity = AlarmEntity(
                id = if (state.isEditMode) alarmId ?: 0 else 0,
                hour = state.hour,
                minute = state.minute,
                isEnabled = state.isEnabled,
                isDailyReminder = state.isDailyReminder,
                reminderFrequencyMinutes = state.reminderFrequencyMinutes,
                ringCount = state.ringCount,
                alarmMusicUri = state.alarmMusicUri,
                alarmMusicDisplayName = state.alarmMusicDisplayName,
                isHolidaySkipped = state.isHolidaySkipped,
                excludedDaysBitmask = state.excludedDaysBitmask,
                label = state.label,
                isVibrate = state.isVibrate,
                isQuietTimeEnabled = state.isQuietTimeEnabled,
                quietStartHour = state.quietStartHour,
                quietStartMinute = state.quietStartMinute,
                quietEndHour = state.quietEndHour,
                quietEndMinute = state.quietEndMinute,
                updatedAt = System.currentTimeMillis()
            )

            if (state.isEditMode) {
                alarmRepository.updateAlarm(entity)
            } else {
                alarmRepository.insertAlarm(entity)
            }

            onSuccess()
        }
    }
}
