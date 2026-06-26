package co.growthmap.alarm.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.growthmap.alarm.scan.BarcodeScanner
import co.growthmap.alarm.ui.theme.AlarmColors
import co.growthmap.alarm.ui.theme.GlassBackground
import co.growthmap.alarm.ui.theme.GlassCard
import co.growthmap.alarm.ui.theme.GlowButton

/**
 * Full-screen Alarm Session UI shown over the lock screen (ADR 0001).
 * Dismissal requires Target Scan + Charging Gate (glossary); no snooze (ADR 0008).
 * After 30 min the Backstop unlock appears (ADR 0005).
 */
class RingActivity : ComponentActivity() {

    private var dismissed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        setContent {
            RingScreen(onDismissed = {
                dismissed = true
                AlarmService.stop(this)
                finish()
            })
        }
    }

    // If the user presses Home/Recents while the alarm is still ringing, bring the
    // ring screen back to the front. The only legitimate exits are meeting the
    // Dismissal Conditions or the 30-min backstop (ADR 0005/0008).
    override fun onStop() {
        super.onStop()
        if (!dismissed && !isFinishing) {
            val intent = Intent(this, RingActivity::class.java).apply {
                putExtra(AlarmScheduler.EXTRA_ALARM_ID, AlarmSession.state.value.alarmId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    // Block back; the only way out is meeting the conditions or the backstop.
    @Deprecated("Intentionally swallow back press during an Alarm Session")
    override fun onBackPressed() { /* no-op */ }
}

@Composable
private fun RingScreen(onDismissed: () -> Unit) {
    val state by AlarmSession.state.collectAsState()

    LaunchedEffect(state.canDismiss) {
        if (state.canDismiss) onDismissed()
    }

    GlassBackground {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                if (state.label.isBlank()) "Wake up" else state.label,
                color = AlarmColors.TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Scan your object to stop the alarm",
                color = AlarmColors.TextSecondary, fontSize = 15.sp
            )
            Spacer(Modifier.height(20.dp))

            // Live scanner with a glowing emerald frame.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .shadow(28.dp, RoundedCornerShape(24.dp), spotColor = AlarmColors.Emerald, ambientColor = AlarmColors.Emerald)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(BorderStroke(1.5.dp, AlarmColors.GlowStroke), RoundedCornerShape(24.dp))
            ) {
                if (!state.scanned) {
                    BarcodeScanner { value ->
                        // Complete the scan condition only on an exact match to the
                        // registered target. A null/blank target can never match, so an
                        // unconfigured alarm cannot be dismissed by a stray barcode.
                        val target = state.targetCode
                        if (!target.isNullOrBlank() && value == target) {
                            AlarmSession.update { it.copy(scanned = true) }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CheckCircle, null,
                            tint = AlarmColors.Emerald, modifier = Modifier.size(96.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            ConditionRow(
                done = state.scanned,
                label = "Scan the configured object",
                icon = Icons.Default.QrCodeScanner
            )
            if (state.requireCharger) {
                Spacer(Modifier.height(12.dp))
                ConditionRow(
                    done = state.charging,
                    label = "Plug phone into a charger",
                    icon = Icons.Default.BatteryChargingFull
                )
            }

            Spacer(Modifier.weight(1f))

            if (state.backstopAvailable) {
                GlowButton(text = "Stop anyway (30-min backstop)", onClick = onDismissed)
            } else {
                val remaining = ((AlarmSession.BACKSTOP_MILLIS - state.elapsedMillis) / 60000).coerceAtLeast(0)
                Text(
                    "Emergency stop available in ${remaining + 1} min",
                    color = AlarmColors.TextMuted, fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ConditionRow(done: Boolean, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glow = done,
        cornerRadius = 18,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(icon, null, tint = if (done) AlarmColors.Emerald else AlarmColors.TextMuted)
            Spacer(Modifier.size(12.dp))
            Text(label, color = AlarmColors.TextPrimary, modifier = Modifier.weight(1f))
            Icon(
                if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                null,
                tint = if (done) AlarmColors.Emerald else AlarmColors.TextMuted
            )
        }
    }
}
