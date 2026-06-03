package com.example.stormpilot.features.dashboard.ui.aichat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import com.example.stormpilot.features.assistant.presentation.GenAIViewModel

@Composable
fun AIChat(title: String, genAIViewModel: GenAIViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "infinite")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "angle"
        )

        // The Popup Container
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Slightly smaller width to allow more shadow room
                .fillMaxHeight(0.75f) // Slightly smaller height
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawWithCache {
                        val androidColors = intArrayOf(
                            android.graphics.Color.parseColor("#4285F4"),
                            android.graphics.Color.parseColor("#EA4335"),
                            android.graphics.Color.parseColor("#FBBC05"),
                            android.graphics.Color.parseColor("#34A853"),
                            android.graphics.Color.parseColor("#4285F4")
                        )
                        
                        onDrawBehind {
                            val shader = android.graphics.SweepGradient(
                                size.width / 2f,
                                size.height / 2f,
                                androidColors,
                                null
                            )
                            val matrix = android.graphics.Matrix()
                            matrix.setRotate(angle, size.width / 2f, size.height / 2f)
                            shader.setLocalMatrix(matrix)

                            val paint = androidx.compose.ui.graphics.Paint().apply {
                                isAntiAlias = true
                                this.shader = shader
                            }
                            
                            val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
                                isAntiAlias = true
                                this.shader = shader
                            }.apply {
                                asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(
                                    20.dp.toPx(),
                                    android.graphics.BlurMaskFilter.Blur.NORMAL
                                )
                            }
                            
                            val cornerRadius = 24.dp.toPx()
                            val rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)

                            drawIntoCanvas { canvas ->
                                // Draw the shadow
                                canvas.drawRoundRect(
                                    left = rect.left,
                                    top = rect.top,
                                    right = rect.right,
                                    bottom = rect.bottom,
                                    radiusX = cornerRadius,
                                    radiusY = cornerRadius,
                                    paint = shadowPaint
                                )
                                // Draw the sharp border
                                canvas.drawRoundRect(
                                    left = rect.left,
                                    top = rect.top,
                                    right = rect.right,
                                    bottom = rect.bottom,
                                    radiusX = cornerRadius,
                                    radiusY = cornerRadius,
                                    paint = paint
                                )
                            }
                        }
                    }
            )

            // The actual content surface
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(1.5.dp) // Thinner 1.5.dp border
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(22.5.dp)) // Dark background for the popup
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    // Additional chat UI could go here
                }
            }
        }
    }
}
