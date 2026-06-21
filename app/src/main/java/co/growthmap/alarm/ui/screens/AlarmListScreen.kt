package co.growthmap.alarm.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.growthmap.alarm.data.AlarmEntity
import co.growthmap.alarm.data.Weekdays
import co.growthmap.alarm.ui.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    vm: AlarmViewModel,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    banner: @Composable () -> Unit = {},
) {
    val alarms by vm.alarms.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Alarms") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, "Add alarm") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            banner()
            if (alarms.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No alarms yet. Tap + to add one.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
    }
}

@Composable
private fun AlarmRow(alarm: AlarmEntity, onToggle: (Boolean) -> Unit, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp).alpha(if (alarm.enabled) 1f else 0.45f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "%02d:%02d".format(alarm.hour, alarm.minute),
                        fontSize = 34.sp, fontWeight = FontWeight.Bold
                    )
                    if (!alarm.isConfigured) {
                        Spacer(Modifier.size(8.dp))
                        Icon(
                            Icons.Default.Warning, "Not configured",
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(repeatSummary(alarm), color = MaterialTheme.colorScheme.outline)
                if (alarm.label.isNotBlank()) {
                    Text(alarm.label, color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                }
            }
            Switch(checked = alarm.enabled, onCheckedChange = onToggle)
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
