package co.growthmap.alarm.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Full-screen emerald gradient with a soft radial glow, used as the base of every screen. */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val vertical = Brush.verticalGradient(
        0f to AlarmColors.BgTop,
        0.5f to AlarmColors.BgMid,
        1f to AlarmColors.BgBottom,
    )
    // A radial emerald glow blooming from upper-center.
    val glow = ShaderBrush(object : ShaderBrush() {
        override fun createShader(size: androidx.compose.ui.geometry.Size): Shader =
            RadialGradientShader(
                center = Offset(size.width * 0.5f, size.height * 0.28f),
                radius = size.maxDimension * 0.7f,
                colors = listOf(Color(0x332EE6A6), Color(0x00000000)),
            )
    })
    Box(modifier.fillMaxSize().background(vertical)) {
        Box(Modifier.fillMaxSize().background(glow))
        content()
    }
}

/** Frosted translucent card with a hairline border. Set [glow] for the emerald glow border. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glow: Boolean = false,
    cornerRadius: Int = 22,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    Box(
        modifier
            .clip(shape)
            .background(if (glow) AlarmColors.GlassFillStrong else AlarmColors.GlassFill)
            .border(
                BorderStroke(1.dp, if (glow) AlarmColors.GlowStroke else AlarmColors.GlassStroke),
                shape
            )
            .padding(contentPadding)
    ) { content() }
}

/** Section label in the muted secondary color. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        color = AlarmColors.TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}
