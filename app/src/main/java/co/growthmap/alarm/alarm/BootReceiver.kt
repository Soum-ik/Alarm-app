package co.growthmap.alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.growthmap.alarm.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Re-arms all enabled alarms after a reboot (ADR 0004: reliable, not a prison). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = AlarmRepository.from(context)
                AlarmScheduler.rescheduleAll(context, repo.getEnabled())
            } finally {
                pending.finish()
            }
        }
    }
}
