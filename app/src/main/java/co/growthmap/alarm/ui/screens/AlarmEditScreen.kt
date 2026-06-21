package co.growthmap.alarm.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import co.growthmap.alarm.data.AlarmEntity
import co.growthmap.alarm.data.Weekdays
import co.growthmap.alarm.scan.BarcodeScanner
import co.growthmap.alarm.ui.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    vm: AlarmViewModel,
    alarmId: Long?,
    hasCameraPermission: Boolean,
    onRequestCamera: () -> Unit,
    onDone: () -> Unit,
) {
    var loaded by remember { mutableStateOf(alarmId == null) }
    var label by remember { mutableStateOf("") }
    var daysMask by remember { mutableStateOf(0) }
    var requireCharger by remember { mutableStateOf(true) }
    var targetCode by remember { mutableStateOf<String?>(null) }
    var targetLabel by remember { mutableStateOf<String?>(null) }
    var enabled by remember { mutableStateOf(true) }
    val timeState = rememberTimePickerState(is24Hour = true)

    LaunchedEffect(alarmId) {
        if (alarmId != null) {
            vm.load(alarmId)?.let { a ->
                label = a.label
                daysMask = a.repeatDaysMask
                requireCharger = a.requireCharger
                targetCode = a.targetCode
                targetLabel = a.targetLabel
                enabled = a.enabled
                timeState.hour = a.hour
                timeState.minute = a.minute
            }
            loaded = true
        }
    }

    var scanning by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarmId == null) "New alarm" else "Edit alarm") },
                actions = {
                    TextButton(
                        enabled = targetCode != null,
                        onClick = {
                            vm.save(
                                AlarmEntity(
                                    id = alarmId ?: 0L,
                                    label = label,
                                    hour = timeState.hour,
                                    minute = timeState.minute,
                                    repeatDaysMask = daysMask,
                                    enabled = enabled,
                                    targetCode = targetCode,
                                    targetLabel = targetLabel,
                                    requireCharger = requireCharger,
                                )
                            )
                            onDone()
                        }
                    ) { Text("Save") }
                }
            )
        }
    ) { padding ->
        if (!loaded) return@Scaffold

        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TimePicker(state = timeState)

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Repeat", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Weekdays.labels.forEachIndexed { i, day ->
                    FilterChip(
                        selected = Weekdays.isSet(daysMask, i),
                        onClick = { daysMask = Weekdays.toggle(daysMask, i) },
                        label = { Text(day.take(1)) }
                    )
                }
            }
            Text(
                if (daysMask == 0) "Fires once, then disables itself"
                else "Repeats on selected days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            // Charging Gate toggle (ADR 0003)
            Card {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Require charger to stop", fontWeight = FontWeight.Medium)
                        Text(
                            "Phone must be plugged in when you dismiss",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(checked = requireCharger, onCheckedChange = { requireCharger = it })
                }
            }

            // Target object registration (ADR 0006: scan an existing barcode)
            Text("Object to scan", style = MaterialTheme.typography.titleMedium)
            Card {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        if (targetCode == null) {
                            Text("Not set", color = MaterialTheme.colorScheme.error)
                            Text(
                                "Scan a barcode on something you own (e.g. a shampoo bottle, a book). You'll scan this exact thing to turn the alarm off.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E9E6B))
                                Spacer(Modifier.size(8.dp))
                                Text(targetLabel ?: "Object registered", fontWeight = FontWeight.Medium)
                            }
                            Text(
                                "Code: ${targetCode!!.take(20)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    OutlinedButton(onClick = {
                        if (hasCameraPermission) scanning = true else onRequestCamera()
                    }) { Text(if (targetCode == null) "Scan" else "Change") }
                }
            }
        }
    }

    if (scanning) {
        ScanDialog(
            onDismiss = { scanning = false },
            onScanned = { code ->
                targetCode = code
                targetLabel = label.ifBlank { "My object" }
                scanning = false
            }
        )
    }
}

@Composable
private fun ScanDialog(onDismiss: () -> Unit, onScanned: (String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Point at the barcode", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier.fillMaxWidth().height(320.dp)
                        .padding(0.dp)
                ) {
                    BarcodeScanner(onBarcode = onScanned)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel")
                }
            }
        }
    }
}
