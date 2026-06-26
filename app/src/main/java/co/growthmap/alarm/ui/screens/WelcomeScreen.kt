package co.growthmap.alarm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.growthmap.alarm.R
import co.growthmap.alarm.ui.theme.AlarmColors
import co.growthmap.alarm.ui.theme.GlassBackground
import co.growthmap.alarm.ui.theme.GlowButton

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    GlassBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing emerald orb behind the icon.
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(132.dp)
                        .shadow(40.dp, CircleShape, spotColor = AlarmColors.Emerald, ambientColor = AlarmColors.Emerald)
                        .clip(CircleShape)
                        .background(AlarmColors.GlassFillStrong)
                )
                Icon(
                    Icons.Default.NotificationsActive, null,
                    tint = AlarmColors.Emerald,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(Modifier.height(36.dp))
            Text(
                stringResource(R.string.welcome_title),
                color = AlarmColors.TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.welcome_body),
                color = AlarmColors.TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
            )
            Spacer(Modifier.height(44.dp))
            GlowButton(text = stringResource(R.string.welcome_cta), onClick = onContinue)
        }
    }
}
