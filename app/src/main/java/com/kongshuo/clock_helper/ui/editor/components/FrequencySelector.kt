package com.kongshuo.clock_helper.ui.editor.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FrequencySelector(
    isDailyReminder: Boolean,
    reminderFrequencyMinutes: Int,
    onDailyChange: (Boolean) -> Unit,
    onFrequencyChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "提醒方式",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(
                selected = isDailyReminder,
                onClick = { onDailyChange(true) },
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "每日提醒",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(
                selected = !isDailyReminder,
                onClick = { onDailyChange(false) },
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "频率提醒",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (!isDailyReminder) {
            OutlinedTextField(
                value = if (reminderFrequencyMinutes > 0) reminderFrequencyMinutes.toString() else "",
                onValueChange = { value ->
                    val minutes = value.toIntOrNull() ?: 0
                    onFrequencyChange(minutes.coerceIn(0, 1440))
                },
                label = { Text("间隔分钟数") },
                singleLine = true,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        }
    }
}
