package co.growthmap.alarm.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.growthmap.alarm.scan.BarcodeScanner

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

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1B2F))
            .padding(24.dp)
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(8.dp))
            Text(
                if (state.label.isBlank()) "Wake up" else state.label,
                color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Scan your object to stop the alarm",
                color = Color(0xFFB9B9D6), fontSize = 15.sp
            )
            Spacer(Modifier.height(16.dp))

            // Live scanner fills the middle.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
            ) {
                if (!state.scanned) {
                    BarcodeScanner { value ->
                        if (value == state.targetCode) {
                            AlarmSession.update { it.copy(scanned = true) }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CheckCircle, null,
                            tint = Color(0xFF5CE6A1), modifier = Modifier.size(96.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            ConditionRow(
                done = state.scanned,
                label = "Scan the configured object",
                icon = Icons.Default.QrCodeScanner
            )
            if (state.requireCharger) {
                Spacer(Modifier.height(10.dp))
                ConditionRow(
                    done = state.charging,
                    label = "Plug phone into a charger",
                    icon = Icons.Default.BatteryChargingFull
                )
            }

            Spacer(Modifier.weight(1f))

            if (state.backstopAvailable) {
                Button(
                    onClick = onDismissed,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Stop anyway (30-min backstop)") }
            } else {
                val remaining = ((AlarmSession.BACKSTOP_MILLIS - state.elapsedMillis) / 60000).coerceAtLeast(0)
                Text(
                    "Emergency stop available in ${remaining + 1} min",
                    color = Color(0xFF6E6E8F), fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ConditionRow(done: Boolean, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (done) Color(0xFF173D2C) else Color(0xFF26263F)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(icon, null, tint = if (done) Color(0xFF5CE6A1) else Color(0xFF9A9AC0))
            Spacer(Modifier.size(12.dp))
            Text(label, color = Color.White, modifier = Modifier.weight(1f))
            Icon(
                if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                null,
                tint = if (done) Color(0xFF5CE6A1) else Color(0xFF55557A)
            )
        }
    }
}
