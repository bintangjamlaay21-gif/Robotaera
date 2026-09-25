package com.aera.robot.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aera.robot.robot.RobotState
import com.aera.robot.ui.theme.NeonCyan
import kotlinx.coroutines.delay

@Composable
fun RobotFace(
    state: RobotState,
    modifier: Modifier = Modifier
) {
    var isBlinking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3200 + (0..1500).random().toLong())
            isBlinking = true
            delay(120)
            isBlinking = false
        }
    }

    val targetEyeHeight = when {
        isBlinking || state == RobotState.SLEEPING -> 6f
        state == RobotState.HAPPY -> 28f
        state == RobotState.SURPRISED -> 58f
        state == RobotState.LISTENING -> 48f
        state == RobotState.THINKING -> 36f
        state == RobotState.SPEAKING -> 40f
        else -> 44f
    }

    val eyeHeight by animateFloatAsState(
        targetValue = targetEyeHeight,
        animationSpec = tween(durationMillis = 180),
        label = "eyeHeight"
    )

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val eyeWidth = 44.dp.toPx()
            val eyeH = eyeHeight.dp.toPx()
            val eyeRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
            val spacing = 36.dp.toPx()

            val leftEyeX = (canvasWidth / 2) - spacing - (eyeWidth / 2)
            val rightEyeX = (canvasWidth / 2) + spacing - (eyeWidth / 2)
            val eyeY = (canvasHeight / 2) - (eyeH / 2)

            // Left eye
            drawRoundRect(
                color = NeonCyan,
                topLeft = Offset(leftEyeX, eyeY),
                size = Size(eyeWidth, eyeH),
                cornerRadius = eyeRadius
            )

            // Right eye (slight offset when confused)
            val rightEyeY = if (state == RobotState.CONFUSED) eyeY - 12.dp.toPx() else eyeY
            drawRoundRect(
                color = NeonCyan,
                topLeft = Offset(rightEyeX, rightEyeY),
                size = Size(eyeWidth, eyeH),
                cornerRadius = eyeRadius
            )

            // Soft glow effect under eyes when active
            if (state != RobotState.SLEEPING && !isBlinking) {
                val glowColor = NeonCyan.copy(alpha = 0.15f)
                drawRoundRect(
                    color = glowColor,
                    topLeft = Offset(leftEyeX - 4.dp.toPx(), eyeY + eyeH + 4.dp.toPx()),
                    size = Size(eyeWidth + 8.dp.toPx(), 8.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                drawRoundRect(
                    color = glowColor,
                    topLeft = Offset(rightEyeX - 4.dp.toPx(), rightEyeY + eyeH + 4.dp.toPx()),
                    size = Size(eyeWidth + 8.dp.toPx(), 8.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
    }
}
