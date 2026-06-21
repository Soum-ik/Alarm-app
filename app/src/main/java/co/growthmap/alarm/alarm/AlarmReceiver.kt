package co.growthmap.alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import co.growthmap.alarm.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Fired at the scheduled time. Starts the Alarm Session service and re-arms recurring alarms. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        if (alarmId < 0) return

        // Start the foreground service that owns the ringing state.
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        ContextCompat.startForegroundService(context, serviceIntent)

        // Re-arm the next occurrence (recurring) so the chain never breaks.
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = AlarmRepository.from(context)
                val alarm = repo.getById(alarmId)
                if (alarm != null && !alarm.isOneShot && alarm.enabled) {
                    AlarmScheduler.reschedule(context, alarm)
                } else if (alarm != null && alarm.isOneShot) {
                    // One-shot fired: disable it so the list reflects reality.
                    repo.upsert(alarm.copy(enabled = false))
                }
            } finally {
                pending.finish()
            }
        }
    }
}
