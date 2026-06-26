package co.growthmap.alarm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.growthmap.alarm.data.AlarmEntity
import co.growthmap.alarm.data.Weekdays
import co.growthmap.alarm.ui.AlarmViewModel
import co.growthmap.alarm.ui.theme.AlarmColors
import co.growthmap.alarm.ui.theme.GlassBackground
import co.growthmap.alarm.ui.theme.GlassCard

@Composable
fun AlarmListScreen(
    vm: AlarmViewModel,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    banner: @Composable () -> Unit = {},
) {
    val alarms by vm.alarms.collectAsState()

    GlassBackground {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                Spacer(Modifier.size(28.dp))
                Text(
                    "Alarms",
                    color = AlarmColors.TextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.size(16.dp))
                banner()

                if (alarms.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No alarms yet.\nTap + to add one.",
                            color = AlarmColors.TextMuted,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
                    ) {
                        items(alarms, key = { it.id }) { alarm ->
                            AlarmRow(
                                alarm = alarm,
                                onToggle = { vm.toggle(alarm, it) },
                                onClick = { onEdit(alarm.id) }
                            )
                        }
                    }
                }
            }

            // Glowing emerald FAB.
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .size(60.dp)
                    .shadow(20.dp, CircleShape, spotColor = AlarmColors.Emerald, ambientColor = AlarmColors.Emerald)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(AlarmColors.Emerald, AlarmColors.EmeraldDeep)))
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, "Add alarm", tint = AlarmColors.BgTop, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun AlarmRow(alarm: AlarmEntity, onToggle: (Boolean) -> Unit, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        glow = alarm.enabled,
    ) {
        Row(
            Modifier.fillMaxWidth().alpha(if (alarm.enabled) 1f else 0.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "%02d:%02d".format(alarm.hour, alarm.minute),
                        color = AlarmColors.TextPrimary,
                        fontSize = 38.sp, fontWeight = FontWeight.Bold
                    )
                    if (!alarm.isConfigured) {
                        Spacer(Modifier.size(8.dp))
                        Icon(
                            Icons.Default.Warning, "Not configured",
                            tint = AlarmColors.Danger, modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(repeatSummary(alarm), color = AlarmColors.TextSecondary, fontSize = 13.sp)
                if (alarm.label.isNotBlank()) {
                    Text(alarm.label, color = AlarmColors.TextMuted, fontSize = 12.sp)
                }
            }
            Switch(
                checked = alarm.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AlarmColors.BgTop,
                    checkedTrackColor = AlarmColors.Emerald,
                    uncheckedThumbColor = AlarmColors.TextMuted,
                    uncheckedTrackColor = Color(0x22FFFFFF),
                    uncheckedBorderColor = Color(0x33FFFFFF),
                )
            )
        }
    }
}

private fun repeatSummary(alarm: AlarmEntity): String {
    if (alarm.isOneShot) return "Once"
    val days = Weekdays.labels.filterIndexed { i, _ -> Weekdays.isSet(alarm.repeatDaysMask, i) }
    return when {
        days.size == 7 -> "Every day"
        days == listOf("Mon", "Tue", "Wed", "Thu", "Fri") -> "Weekdays"
        days == listOf("Sat", "Sun") -> "Weekends"
        else -> days.joinToString(" ")
    }
}
