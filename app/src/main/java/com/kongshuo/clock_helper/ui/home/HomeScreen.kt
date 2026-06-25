package com.kongshuo.clock_helper.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kongshuo.clock_helper.ui.home.components.AlarmCard
import com.kongshuo.clock_helper.ui.home.components.EmptyStateView
import com.kongshuo.clock_helper.ui.home.components.NextAlarmBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreateAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    val nextAlarm by viewModel.nextAlarm.collectAsState()
    val nextAlarmTime by viewModel.nextAlarmTime.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("闹钟") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateAlarm,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "创建闹钟",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        if (alarms.isEmpty()) {
            EmptyStateView(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 下一个闹钟横幅
                if (nextAlarm != null) {
                    item {
                        NextAlarmBanner(
                            alarm = nextAlarm,
                            fireTimeMillis = nextAlarmTime
                        )
                    }
                }

                // 闹钟列表
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onEdit = { onEditAlarm(alarm.id) },
                        onDelete = { viewModel.deleteAlarm(alarm) }
                    )
                }
            }
        }
    }
}
