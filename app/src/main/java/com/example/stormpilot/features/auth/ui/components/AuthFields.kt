package com.example.stormpilot.features.auth.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthModeSwitchRow(
    prompt: String,
    actionLabel: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
            color = scheme.onSurfaceVariant,
        )

        TextButton(onClick = onClick) {
            Text(
                text = actionLabel,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

/**
 * Shared field styling keeps auth inputs consistent across sign-in and sign-up
 * without burying the screen flow inside a huge monolithic file.
 */
@Composable
fun ExpressiveInputField(
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val glowColor by animateColorAsState(
        targetValue = if (isFocused) scheme.primary.copy(alpha = 0.34f) else Color.Transparent,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "input_glow_color",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) {
            scheme.primary.copy(alpha = 0.55f)
        } else {
            scheme.outlineVariant.copy(alpha = 0.18f)
        },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "input_border_color",
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 18.dp else 0.dp,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "input_shadow_elevation",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = scheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        Surface(
            modifier = Modifier
                .shadow(
                    elevation = shadowElevation,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = glowColor,
                    ambientColor = glowColor,
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(24.dp),
                ),
            shape = RoundedCornerShape(24.dp),
            color = scheme.surfaceBright.copy(alpha = 0.86f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 7.dp)
                    .heightIn(min = 56.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(scheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = scheme.primary,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    visualTransformation = visualTransformation,
                    textStyle = TextStyle(
                        color = scheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    cursorBrush = SolidColor(scheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (placeholder != null && value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = scheme.outline,
                                    ),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = scheme.outlineVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
fun LinedIconInputField(
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    ExpressiveInputField(
        value = value,
        onValueChange = onValueChange,
        icon = icon,
        label = "",
        modifier = modifier,
        placeholder = placeholder,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
    )
}
