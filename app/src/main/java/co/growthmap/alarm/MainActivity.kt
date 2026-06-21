package co.growthmap.alarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.growthmap.alarm.ui.AlarmViewModel
import co.growthmap.alarm.ui.screens.AlarmEditScreen
import co.growthmap.alarm.ui.screens.AlarmListScreen
import co.growthmap.alarm.ui.screens.WelcomeScreen
import co.growthmap.alarm.ui.theme.AlarmTheme
import co.growthmap.alarm.util.Permissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNav()
                }
            }
        }
    }
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    val vm: AlarmViewModel = viewModel()
    val context = LocalContext.current

    var hasCamera by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCamera = granted }

    // Ask for notifications on Android 13+ so the full-screen alarm shows reliably.
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    NavHost(navController = nav, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(onContinue = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                nav.navigate("list") { popUpTo("welcome") { inclusive = true } }
            })
        }
        composable("list") {
            AlarmListScreen(
                vm = vm,
                onAdd = { nav.navigate("edit") },
                onEdit = { id -> nav.navigate("edit?id=$id") },
                banner = { ReliabilityGate() }
            )
        }
        composable("edit") {
            AlarmEditScreen(
                vm = vm, alarmId = null,
                hasCameraPermission = hasCamera,
                onRequestCamera = { cameraLauncher.launch(Manifest.permission.CAMERA) },
                onDone = { nav.popBackStack() }
            )
        }
        composable("edit?id={id}") { backStack ->
            val id = backStack.arguments?.getString("id")?.toLongOrNull()
            AlarmEditScreen(
                vm = vm, alarmId = id,
                hasCameraPermission = hasCamera,
                onRequestCamera = { cameraLauncher.launch(Manifest.permission.CAMERA) },
                onDone = { nav.popBackStack() }
            )
        }
    }
}

/** Surfaces the exact-alarm + battery-exemption prompts the app needs to fire reliably (ADR 0004). */
@Composable
private fun ReliabilityGate() {
    val context = LocalContext.current
    val needsExact = !Permissions.canScheduleExactAlarms(context)
    val needsBattery = !Permissions.isIgnoringBatteryOptimizations(context)
    if (!needsExact && !needsBattery) return

    Column(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "For alarms to fire reliably, grant these:",
            style = MaterialTheme.typography.bodySmall
        )
        if (needsExact) {
            Button(
                onClick = { Permissions.exactAlarmSettingsIntent(context)?.let { context.startActivity(it) } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Allow exact alarms") }
        }
        if (needsBattery) {
            Button(
                onClick = { context.startActivity(Permissions.batteryExemptionIntent(context)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ignore battery optimizations") }
        }
    }
}
