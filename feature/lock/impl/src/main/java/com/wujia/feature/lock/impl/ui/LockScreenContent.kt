package com.wujia.feature.lock.impl.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.scene.SceneResource
import com.wujia.foundation.player.VelarisVideoPlayer
import com.wujia.foundation.player.rememberVelarisPlayerController
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun LockScreenContent(
    scene: SceneResource?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    var now by remember { mutableStateOf(currentLocalDateTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = currentLocalDateTime()
            delay(1_000L)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "arrow")
    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "arrowAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(onDismiss) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -80) {
                        onDismiss()
                    }
                }
            },
    ) {
        // Layer 1: scene video or gradient fallback
        if (scene?.video != null) {
            val controller = rememberVelarisPlayerController(
                videoUri = scene.video!!.uri,
                playWhenReady = true,
            )
            VelarisVideoPlayer(
                controller = controller,
                modifier = Modifier.fillMaxSize(),
            )
            // dimming overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0D1117),
                                Color(0xFF161B22),
                                Color(0xFF0D1117),
                            ),
                        ),
                    ),
            )
        }

        // Layer 2: scene title
        if (scene != null) {
            Text(
                text = scene.title,
                fontSize = spec.typography.title,
                color = spec.colors.textSecondary,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 32.dp, top = 24.dp),
            )
        }

        // Layer 3: clock + date centered
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = now.time,
                fontSize = spec.typography.display,
                fontWeight = FontWeight.Light,
                color = spec.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = now.date,
                fontSize = spec.typography.subtitle,
                color = spec.colors.textSecondary,
            )
        }

        // Layer 4: bottom swipe hint
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = spec.colors.textSecondary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "▲  上滑解锁",
                fontSize = spec.typography.label,
                color = spec.colors.textSecondary.copy(alpha = arrowAlpha),
            )
        }
    }
}

private data class LocalDateTime(val time: String, val date: String)

private fun currentLocalDateTime(): LocalDateTime {
    val now = Clock.System.now()
    val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val time = "%02d:%02d".format(local.hour, local.minute)
    val date = "%d/%02d/%02d  %s".format(
        local.year,
        local.monthNumber,
        local.dayOfMonth,
        dayOfWeekCn(local.dayOfWeek.value),
    )
    return LocalDateTime(time, date)
}

private fun dayOfWeekCn(isoDay: Int): String = when (isoDay) {
    1 -> "周一"
    2 -> "周二"
    3 -> "周三"
    4 -> "周四"
    5 -> "周五"
    6 -> "周六"
    7 -> "周日"
    else -> ""
}
