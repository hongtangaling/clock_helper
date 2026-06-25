package com.kongshuo.clock_helper.ui.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kongshuo.clock_helper.data.entity.AlarmEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayExclusionGrid(
    excludedDaysBitmask: Int,
    onExcludedDaysChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf(
        "周日" to 1,
        "周一" to 2,
        "周二" to 3,
        "周三" to 4,
        "周四" to 5,
        "周五" to 6,
        "周六" to 7
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "排除日期",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            days.forEach { (label, dayOfWeek) ->
                val isExcluded = AlarmEntity.isDayExcluded(excludedDaysBitmask, dayOfWeek)
                FilterChip(
                    selected = isExcluded,
                    onClick = {
                        onExcludedDaysChange(
                            AlarmEntity.setDayExcluded(excludedDaysBitmask, dayOfWeek, !isExcluded)
                        )
                    },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
