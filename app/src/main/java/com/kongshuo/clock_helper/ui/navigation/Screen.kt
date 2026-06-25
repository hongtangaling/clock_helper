package com.kongshuo.clock_helper.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object CreateAlarm : Screen("create_alarm")
    data object EditAlarm : Screen("edit_alarm/{alarmId}") {
        fun createRoute(alarmId: Long) = "edit_alarm/$alarmId"
    }
    data object Settings : Screen("settings")
}
