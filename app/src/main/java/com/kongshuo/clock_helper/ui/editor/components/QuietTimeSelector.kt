package com.kongshuo.clock_helper.ui.editor.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kongshuo.clock_helper.data.entity.QuietTimePeriod

@Composable
fun QuietTimeSelector(
    periods: List<QuietTimePeriod>,
    onPeriodsChange: (List<QuietTimePeriod>) -> Unit,
    modifier: Modifier = Modifier
) {
    // 正在编辑的时段索引和字段（null=没打开选择器）
    var editingIndex by remember { mutableStateOf(-1) }
    var editingField by remember { mutableStateOf("") } // "start" or "end"

    Column(modifier = modifier.fillMaxWidth()) {
        // 标题行
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "免打扰时段",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.weight(1f))
            // 添加按钮
            if (periods.size < 10) { // 最多 10 个避免界面太拥挤
                TextButton(onClick = {
                    val newPeriod = QuietTimePeriod(22, 0, 8, 0)
                    onPeriodsChange(periods + newPeriod)
                }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加免打扰时段",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加")
                }
            }
        }

        if (periods.isEmpty()) {
            Text(
                text = "未设置免打扰时段",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
            return@Column
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 时段列表
        periods.forEachIndexed { index, period ->
            QuietTimePeriodRow(
                period = period,
                onStartClick = {
                    editingIndex = index
                    editingField = "start"
                },
                onEndClick = {
                    editingIndex = index
                    editingField = "end"
                },
                onDelete = {
                    onPeriodsChange(periods.toMutableList().apply { removeAt(index) })
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }

    // 时间选择器对话框
    if (editingIndex in periods.indices && editingField.isNotEmpty()) {
        val period = periods[editingIndex]
        val initialHour = if (editingField == "start") period.startHour else period.endHour
        val initialMinute = if (editingField == "start") period.startMinute else period.endMinute

        TimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onConfirm = { hour, minute ->
                val updated = periods.toMutableList()
                if (editingField == "start") {
                    updated[editingIndex] = period.copy(startHour = hour, startMinute = minute)
                } else {
                    updated[editingIndex] = period.copy(endHour = hour, endMinute = minute)
                }
                onPeriodsChange(updated)
                editingIndex = -1
                editingField = ""
            },
            onDismiss = {
                editingIndex = -1
                editingField = ""
            }
        )
    }
}

@Composable
private fun QuietTimePeriodRow(
    period: QuietTimePeriod,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // 开始时间
        TimeDisplay(
            label = "开始",
            hour = period.startHour,
            minute = period.startMinute,
            onClick = onStartClick,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "~",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // 结束时间
        TimeDisplay(
            label = "结束",
            hour = period.endHour,
            minute = period.endMinute,
            onClick = onEndClick,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(4.dp))

        // 删除按钮
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "删除此免打扰时段",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TimeDisplay(
    label: String,
    hour: Int,
    minute: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "%02d:%02d".format(hour, minute),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择时间",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = MaterialTheme.colorScheme.primaryContainer,
                    selectorColor = MaterialTheme.colorScheme.primary,
                    clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
