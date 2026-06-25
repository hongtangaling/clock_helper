package com.kongshuo.clock_helper.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * 检查通知权限 (Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    /**
     * 检查精确闹钟权限 (Android 12+)
     */
    fun hasExactAlarmPermission(): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    /**
     * 检查电池优化白名单
     */
    fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * 打开精确闹钟权限设置页面
     */
    fun openExactAlarmSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        activity.startActivity(intent)
    }

    /**
     * 打开电池优化设置页面
     */
    fun openBatteryOptimizationSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        activity.startActivity(intent)
    }

    /**
     * 打开通知权限设置页面（应用详情）
     */
    fun openAppNotificationSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        activity.startActivity(intent)
    }

    /**
     * 获取所有关键权限状态
     */
    data class PermissionStatus(
        val hasNotificationPermission: Boolean,
        val hasExactAlarmPermission: Boolean,
        val isIgnoringBatteryOptimizations: Boolean
    )

    fun getPermissionStatus(): PermissionStatus {
        return PermissionStatus(
            hasNotificationPermission = hasNotificationPermission(),
            hasExactAlarmPermission = hasExactAlarmPermission(),
            isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations()
        )
    }

    /**
     * 检查是否可以保存闹钟（关键权限）
     */
    fun canSaveAlarm(): Boolean {
        return hasNotificationPermission() && hasExactAlarmPermission()
    }
}
