package co.growthmap.alarm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Emerald "glass / glow" design system. Dark, near-black backgrounds with translucent
 * frosted surfaces and a teal-emerald glow accent.
 */
object AlarmColors {
    val Emerald = Color(0xFF2EE6A6)      // primary glow
    val EmeraldDeep = Color(0xFF12C98D)
    val Teal = Color(0xFF1FB6C9)

    // Background gradient stops (top -> bottom)
    val BgTop = Color(0xFF06120F)
    val BgMid = Color(0xFF0A2A22)
    val BgBottom = Color(0xFF04100C)

    val GlassFill = Color(0x14FFFFFF)    // ~8% white, frosted card
    val GlassFillStrong = Color(0x1FFFFFFF)
    val GlassStroke = Color(0x33FFFFFF)  // hairline border
    val GlowStroke = Color(0x662EE6A6)   // emerald glow border

    val TextPrimary = Color(0xFFEAFBF4)
    val TextSecondary = Color(0xFF8FB7AC)
    val TextMuted = Color(0xFF5B7E75)
    val Danger = Color(0xFFFF6B6B)
}

private val EmeraldDark = darkColorScheme(
    primary = AlarmColors.Emerald,
    onPrimary = Color(0xFF04130D),
    secondary = AlarmColors.Teal,
    background = AlarmColors.BgTop,
    onBackground = AlarmColors.TextPrimary,
    surface = AlarmColors.BgMid,
    onSurface = AlarmColors.TextPrimary,
    error = AlarmColors.Danger,
    outline = AlarmColors.TextMuted,
)

@Composable
fun AlarmTheme(content: @Composable () -> Unit) {
    // Always dark — this is a night-time alarm app and the glass look needs a dark base.
    MaterialTheme(colorScheme = EmeraldDark, content = content)
}
