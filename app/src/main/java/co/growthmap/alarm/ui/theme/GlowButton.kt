package co.growthmap.alarm.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Emerald gradient pill button with a soft glow shadow. The signature primary action. */
@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f)
            .shadow(if (enabled) 18.dp else 0.dp, shape, spotColor = AlarmColors.Emerald, ambientColor = AlarmColors.Emerald)
            .clip(shape)
            .background(
                Brush.horizontalGradient(listOf(AlarmColors.Emerald, AlarmColors.EmeraldDeep))
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(PaddingValues(vertical = 16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = AlarmColors.BgTop,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Subtle outlined/ghost button on glass (e.g. "Scan", "Change"). */
@Composable
fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .clip(shape)
            .background(AlarmColors.GlassFillStrong)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = AlarmColors.Emerald, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
