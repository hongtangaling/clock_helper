package com.kongshuo.clock_helper.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kongshuo.clock_helper.ui.editor.components.DayExclusionGrid
import com.kongshuo.clock_helper.ui.editor.components.FrequencySelector
import com.kongshuo.clock_helper.ui.editor.components.HolidaySkipToggle
import com.kongshuo.clock_helper.ui.editor.components.LabelInput
import com.kongshuo.clock_helper.ui.editor.components.MusicSelector
import com.kongshuo.clock_helper.ui.editor.components.QuietTimeSelector
import com.kongshuo.clock_helper.ui.editor.components.RingCountSelector
import com.kongshuo.clock_helper.ui.editor.components.VibrateToggle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: AlarmEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // 使用 rememberTimePickerState 创建时间选择器状态
    val timePickerState = rememberTimePickerState(
        initialHour = state.hour,
        initialMinute = state.minute,
        is24Hour = true
    )

    // 编辑模式下数据加载完成后，同步实际时间到 TimePicker
    if (state.isEditMode) {
        androidx.compose.runtime.LaunchedEffect(state.isDataLoaded) {
            if (state.isDataLoaded) {
                timePickerState.hour = state.hour
                timePickerState.minute = state.minute
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditMode) "编辑闹钟" else "新建闹钟")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 时间选择器
            TimePicker(
                state = timePickerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // 更新时间状态
            androidx.compose.runtime.LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                viewModel.updateHour(timePickerState.hour)
                viewModel.updateMinute(timePickerState.minute)
            }

            HorizontalDivider()

            // 标签
            LabelInput(
                label = state.label,
                onLabelChange = viewModel::updateLabel
            )

            HorizontalDivider()

            // 提醒方式
            FrequencySelector(
                isDailyReminder = state.isDailyReminder,
                reminderFrequencyMinutes = state.reminderFrequencyMinutes,
                onDailyChange = viewModel::updateIsDailyReminder,
                onFrequencyChange = viewModel::updateFrequency
            )

            HorizontalDivider()

            // 排除日期
            if (state.isDailyReminder) {
                DayExclusionGrid(
                    excludedDaysBitmask = state.excludedDaysBitmask,
                    onExcludedDaysChange = viewModel::updateExcludedDays
                )
                HorizontalDivider()
            }

            // 跳过节假日
            HolidaySkipToggle(
                isHolidaySkipped = state.isHolidaySkipped,
                onHolidaySkipChange = viewModel::updateHolidaySkip
            )

            HorizontalDivider()

            // 铃声选择
            MusicSelector(
                musicUri = state.alarmMusicUri,
                musicDisplayName = state.alarmMusicDisplayName,
                onMusicSelected = viewModel::updateMusic
            )

            HorizontalDivider()

            // 响铃次数
            RingCountSelector(
                ringCount = state.ringCount,
                onRingCountChange = viewModel::updateRingCount
            )

            HorizontalDivider()

            // 震动
            VibrateToggle(
                isVibrate = state.isVibrate,
                onVibrateChange = viewModel::updateVibrate
            )

            HorizontalDivider()

            // 免打扰时段
            QuietTimeSelector(
                isEnabled = state.isQuietTimeEnabled,
                startHour = state.quietStartHour,
                startMinute = state.quietStartMinute,
                endHour = state.quietEndHour,
                endMinute = state.quietEndMinute,
                onEnabledChange = viewModel::updateQuietTimeEnabled,
                onStartChange = viewModel::updateQuietStart,
                onEndChange = viewModel::updateQuietEnd
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 保存按钮
            Button(
                onClick = {
                    viewModel.save(onSuccess = onNavigateBack)
                },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (state.isEditMode) "更新闹钟" else "保存闹钟",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (!state.canSave) {
                Text(
                    text = "需要授予通知和精确闹钟权限才能保存闹钟",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
