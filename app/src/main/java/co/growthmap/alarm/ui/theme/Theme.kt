package co.growthmap.alarm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFFFF5C8A)

private val DarkColors = darkColorScheme(
    primary = Accent,
    background = Color(0xFF14141F),
    surface = Color(0xFF1F1F30),
)

private val LightColors = lightColorScheme(
    primary = Accent,
)

@Composable
fun AlarmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
