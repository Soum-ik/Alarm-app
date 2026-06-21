package co.growthmap.alarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import co.growthmap.alarm.data.AlarmEntity
import java.util.Calendar

/**
 * Schedules exact alarms via AlarmManager (ADR 0007). Computes the next occurrence
 * for recurring alarms (ADR 0006) and re-arms reliably.
 */
object AlarmScheduler {

    const val EXTRA_ALARM_ID = "alarm_id"

    fun reschedule(context: Context, alarm: AlarmEntity) {
        cancel(context, alarm.id)
        if (!alarm.enabled) return
        val triggerAt = nextTriggerMillis(alarm) ?: return
        val am = context.getSystemService(AlarmManager::class.java)

        // On Android 12+, exact alarms may require user permission. If not granted,
        // fall back to an inexact alarm rather than crashing (caller should prompt).
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
        val pi = firePendingIntent(context, alarm.id)
        val info = AlarmManager.AlarmClockInfo(triggerAt, showIntent(context, alarm.id))

        if (canExact) {
            // setAlarmClock survives Doze and shows the system next-alarm indicator.
            am.setAlarmClock(info, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(context: Context, alarmId: Long) {
        val am = context.getSystemService(AlarmManager::class.java)
        am.cancel(firePendingIntent(context, alarmId))
    }

    suspend fun rescheduleAll(context: Context, alarms: List<AlarmEntity>) {
        alarms.forEach { reschedule(context, it) }
    }

    /** Next fire time in epoch millis, or null if a one-shot already in the past with no repeat. */
    fun nextTriggerMillis(alarm: AlarmEntity, now: Calendar = Calendar.getInstance()): Long? {
        if (alarm.isOneShot) {
            val c = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            if (c.timeInMillis <= now.timeInMillis) c.add(Calendar.DAY_OF_YEAR, 1)
            return c.timeInMillis
        }
        // Recurring: find the nearest future weekday in the mask.
        for (offset in 0..7) {
            val c = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            // Calendar.MONDAY=2 .. SUNDAY=1 -> map to bit index 0..6 (Mon..Sun)
            val dow = c.get(Calendar.DAY_OF_WEEK)
            val bitIndex = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
            val dayEnabled = (alarm.repeatDaysMask shr bitIndex) and 1 == 1
            if (dayEnabled && c.timeInMillis > now.timeInMillis) return c.timeInMillis
        }
        return null
    }

    private fun firePendingIntent(context: Context, alarmId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context, alarmId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showIntent(context: Context, alarmId: Long): PendingIntent {
        val intent = Intent(context, RingActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getActivity(
            context, alarmId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
