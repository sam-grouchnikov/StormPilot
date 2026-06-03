package com.example.stormpilot.features.navigation.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.coloredShadow(
    color: Color,
    borderRadius: Dp = 50.dp,
    blurRadius: Dp = 20.dp,
    offsetY: Dp = 0.dp,
    spread: Dp = 0.dp,
) = drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                this.color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    blurRadius.toPx(),
                    0f,
                    offsetY.toPx(),
                    color.copy(alpha = 0.5f).toArgb(),
                )
            }
        }
        canvas.drawRoundRect(
            left = -spread.toPx(),
            top = -spread.toPx(),
            right = size.width + spread.toPx(),
            bottom = size.height + spread.toPx(),
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint,
        )
    }
}
