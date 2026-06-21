package co.growthmap.alarm.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import co.growthmap.alarm.MainActivity
import co.growthmap.alarm.R
import co.growthmap.alarm.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground service that owns the ringing Alarm Session: plays sound at full volume,
 * vibrates, tracks charging state, runs the 30-min backstop timer (ADR 0005), and stops
 * only when the session is dismissed (ADR 0008: no snooze).
 */
class AlarmService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var tickJob: Job? = null
    private var startMillis = 0L

    private val chargingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refreshCharging()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L) ?: -1L
        if (alarmId < 0) { stopSelf(); return START_NOT_STICKY }

        startMillis = elapsedNow()
        startForeground(NOTIF_ID, buildNotification("Alarm"))
        acquireWakeLock()
        loadAlarmAndStart(alarmId)
        return START_REDELIVER_INTENT
    }

    private fun loadAlarmAndStart(alarmId: Long) {
        scope.launch {
            val alarm = AlarmRepository.from(this@AlarmService).getById(alarmId)
            AlarmSession.update {
                AlarmSession.State(
                    alarmId = alarmId,
                    targetCode = alarm?.targetCode,
                    requireCharger = alarm?.requireCharger ?: true,
                    label = alarm?.label ?: "",
                    charging = isCharging(),
                    scanned = false,
                    elapsedMillis = 0L,
                )
            }
            launchUi(alarmId)
            startSound()
            registerReceiver(chargingReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            })
            runTicker()
        }
    }

    private fun launchUi(alarmId: Long) {
        val intent = Intent(this, RingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    /** Polls elapsed time so the backstop unlock (ADR 0005) appears at 30:00. */
    private fun runTicker() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (true) {
                val elapsed = elapsedNow() - startMillis
                AlarmSession.update { it.copy(elapsedMillis = elapsed, charging = isCharging()) }
                if (AlarmSession.state.value.canDismiss) break
                delay(1000)
            }
        }
    }

    private fun refreshCharging() {
        AlarmSession.update { it.copy(charging = isCharging()) }
    }

    private fun isCharging(): Boolean {
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // isCharging is the most direct signal.
            return bm.isCharging
        }
        val status = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun startSound() {
        // Force alarm stream to a loud level so the user can't pre-mute their way out.
        val audio = getSystemService(AUDIO_SERVICE) as AudioManager
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        audio.setStreamVolume(AudioManager.STREAM_ALARM, max, 0)

        val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            isLooping = true
            setDataSource(this@AlarmService, uri)
            prepare()
            start()
        }

        vibrator = (getSystemService(VIBRATOR_SERVICE) as Vibrator).also { v ->
            val pattern = longArrayOf(0, 600, 600)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION") v.vibrate(pattern, 0)
            }
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "alarm:ring"
        ).apply { acquire(AlarmSession.BACKSTOP_MILLIS + 60_000) }
    }

    private fun buildNotification(text: String): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.alarm_channel_desc)
                setSound(null, null) // service plays its own sound
                enableVibration(false)
            }
            nm.createNotificationChannel(channel)
        }
        val full = PendingIntent.getActivity(
            this, 0,
            Intent(this, RingActivity::class.java)
                .putExtra(AlarmScheduler.EXTRA_ALARM_ID, AlarmSession.state.value.alarmId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val content = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(content)
            .setFullScreenIntent(full, true)
            .build()
    }

    override fun onDestroy() {
        tickJob?.cancel()
        runCatching { unregisterReceiver(chargingReceiver) }
        player?.runCatching { stop(); release() }
        player = null
        vibrator?.cancel()
        wakeLock?.let { if (it.isHeld) it.release() }
        AlarmSession.reset()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "alarm_ring"
        private const val NOTIF_ID = 1001
        private fun elapsedNow() = android.os.SystemClock.elapsedRealtime()

        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmService::class.java))
        }
    }
}
