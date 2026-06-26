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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import co.growthmap.alarm.data.AlarmEntity
import co.growthmap.alarm.data.Weekdays
import co.growthmap.alarm.scan.BarcodeScanner
import co.growthmap.alarm.ui.AlarmViewModel
import co.growthmap.alarm.ui.theme.AlarmColors
import co.growthmap.alarm.ui.theme.GhostButton
import co.growthmap.alarm.ui.theme.GlassBackground
import co.growthmap.alarm.ui.theme.GlassCard
import co.growthmap.alarm.ui.theme.GlowButton
import co.growthmap.alarm.ui.theme.SectionLabel

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

    GlassBackground {
        if (!loaded) return@GlassBackground

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.size(8.dp))
            // Header row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AlarmColors.GlassFill)
                        .clickable(onClick = onDone),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = AlarmColors.TextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    if (alarmId == null) "New alarm" else "Edit alarm",
                    color = AlarmColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold
                )
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                TimePicker(
                    state = timeState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = AlarmColors.GlassFill,
                        selectorColor = AlarmColors.Emerald,
                        timeSelectorSelectedContainerColor = AlarmColors.GlassFillStrong,
                        timeSelectorSelectedContentColor = AlarmColors.Emerald,
                        timeSelectorUnselectedContainerColor = AlarmColors.GlassFill,
                        timeSelectorUnselectedContentColor = AlarmColors.TextPrimary,
                        clockDialSelectedContentColor = AlarmColors.BgTop,
                        clockDialUnselectedContentColor = AlarmColors.TextPrimary,
                    )
                )
            }

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AlarmColors.Emerald,
                    unfocusedBorderColor = AlarmColors.GlassStroke,
                    focusedLabelColor = AlarmColors.Emerald,
                    unfocusedLabelColor = AlarmColors.TextMuted,
                    focusedTextColor = AlarmColors.TextPrimary,
                    unfocusedTextColor = AlarmColors.TextPrimary,
                    cursorColor = AlarmColors.Emerald,
                )
            )

            SectionLabel("Repeat")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Weekdays.labels.forEachIndexed { i, day ->
                    FilterChip(
                        selected = Weekdays.isSet(daysMask, i),
                        onClick = { daysMask = Weekdays.toggle(daysMask, i) },
                        label = { Text(day.take(1)) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = AlarmColors.GlassFill,
                            labelColor = AlarmColors.TextSecondary,
                            selectedContainerColor = AlarmColors.Emerald,
                            selectedLabelColor = AlarmColors.BgTop,
                        )
                    )
                }
            }
            Text(
                if (daysMask == 0) "Fires once, then disables itself" else "Repeats on selected days",
                color = AlarmColors.TextMuted, fontSize = 12.sp
            )

            // Charging Gate toggle (ADR 0003)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Require charger to stop", color = AlarmColors.TextPrimary, fontWeight = FontWeight.Medium)
                        Text(
                            "Phone must be plugged in when you dismiss",
                            color = AlarmColors.TextMuted, fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = requireCharger,
                        onCheckedChange = { requireCharger = it },
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

            // Target object registration (ADR 0006)
            SectionLabel("Object to scan")
            GlassCard(modifier = Modifier.fillMaxWidth(), glow = targetCode != null) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        if (targetCode == null) {
                            Text("Not set", color = AlarmColors.Danger, fontWeight = FontWeight.Medium)
                            Text(
                                "Scan a barcode on something you own (a bottle, a book). You'll scan this exact thing to turn the alarm off.",
                                color = AlarmColors.TextMuted, fontSize = 12.sp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = AlarmColors.Emerald, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(targetLabel ?: "Object registered", color = AlarmColors.TextPrimary, fontWeight = FontWeight.Medium)
                            }
                            Text("Code: ${targetCode!!.take(20)}", color = AlarmColors.TextMuted, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.size(12.dp))
                    GhostButton(text = if (targetCode == null) "Scan" else "Change") {
                        if (hasCameraPermission) scanning = true else onRequestCamera()
                    }
                }
            }

            Spacer(Modifier.size(4.dp))
            GlowButton(
                text = "Save alarm",
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
            )
            if (targetCode == null) {
                Text(
                    "Scan an object above to enable saving",
                    color = AlarmColors.TextMuted, fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Spacer(Modifier.size(16.dp))
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
        GlassCard(modifier = Modifier.fillMaxWidth(), glow = true) {
            Column {
                Text("Point at the barcode", color = AlarmColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                ) {
                    BarcodeScanner(onBarcode = onScanned)
                }
                Spacer(Modifier.height(12.dp))
                GhostButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}
