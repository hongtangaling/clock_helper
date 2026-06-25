package com.kongshuo.clock_helper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kongshuo.clock_helper.ui.editor.AlarmEditorScreen
import com.kongshuo.clock_helper.ui.home.HomeScreen
import com.kongshuo.clock_helper.ui.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onCreateAlarm = {
                    navController.navigate(Screen.CreateAlarm.route)
                },
                onEditAlarm = { alarmId ->
                    navController.navigate(Screen.EditAlarm.createRoute(alarmId))
                },
                onSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.CreateAlarm.route) {
            AlarmEditorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditAlarm.route,
            arguments = listOf(
                navArgument("alarmId") { type = NavType.LongType }
            )
        ) {
            AlarmEditorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
