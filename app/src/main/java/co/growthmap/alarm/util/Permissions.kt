package co.growthmap.alarm.util

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/** Helpers for the permission/exemption prompts this app needs (ADR 0004). */
object Permissions {

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(AlarmManager::class.java)
        return am.canScheduleExactAlarms()
    }

    fun exactAlarmSettingsIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(android.os.PowerManager::class.java)
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    @Suppress("BatteryLife")
    fun batteryExemptionIntent(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
}
