package com.kongshuo.clock_helper.ui.editor.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerSection(
    state: TimePickerState,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                "选择时间",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            TimePicker(
                state = state
            )
        },
        confirmButton = { },
        dismissButton = { }
    )
}
