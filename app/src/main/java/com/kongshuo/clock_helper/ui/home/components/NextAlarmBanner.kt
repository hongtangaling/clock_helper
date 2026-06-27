package com.kongshuo.clock_helper.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kongshuo.clock_helper.data.entity.AlarmEntity
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun NextAlarmBanner(
    alarm: AlarmEntity?,
    fireTimeMillis: Long?,
    modifier: Modifier = Modifier
) {
    if (alarm == null || fireTimeMillis == null) return

    // 倒计时实时更新
    var remainingMillis by remember { mutableStateOf(fireTimeMillis - System.currentTimeMillis()) }

    LaunchedEffect(fireTimeMillis) {
        while (true) {
            remainingMillis = fireTimeMillis - System.currentTimeMillis()
            if (remainingMillis <= 0) break
            delay(1000L)
        }
    }

    val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
    // 频率提醒显示实际的下次触发时间，每日提醒显示设定的固定时间
    val timeStr = if (!alarm.isDailyReminder && alarm.reminderFrequencyMinutes > 0) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = fireTimeMillis }
        "%02d:%02d".format(
            cal.get(java.util.Calendar.HOUR_OF_DAY),
            cal.get(java.util.Calendar.MINUTE)
        )
    } else {
        "%02d:%02d".format(alarm.hour, alarm.minute)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column {
                Text(
                    text = "下一个闹钟",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (remainingMillis > 0) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (hours > 0) "${hours}小时${minutes}分钟" else "${minutes}分钟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "后响铃",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
