package com.example.stormpilot.features.auth.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 22.sp,
            ),
            color = Color.White.copy(alpha = 0.68f),
        )

        TextButton(onClick = onClick) {
            Text(
                text = actionLabel,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.primary,
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
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val containerColor by animateColorAsState(
        targetValue = if (isFocused) {
            Color.White.copy(alpha = 0.18f)
        } else {
            Color.White.copy(alpha = 0.10f)
        },
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "input_container_color",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) {
            Color.White.copy(alpha = 0.76f)
        } else {
            Color.White.copy(alpha = 0.24f)
        },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "input_border_color",
    )
    val labelColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color.White.copy(alpha = 0.74f),
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "input_label_color",
    )
    val iconBackgroundColor by animateColorAsState(
        targetValue = if (isFocused) {
            Color.White.copy(alpha = 0.22f)
        } else {
            Color.White.copy(alpha = 0.12f)
        },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "input_icon_background_color",
    )
    val iconTint by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color.White.copy(alpha = 0.72f),
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "input_icon_tint",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "input_icon_scale",
    )
    val shadowElevation = 0.dp

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = labelColor,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Surface(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(18.dp),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    focusRequester.requestFocus()
                },
            shape = RoundedCornerShape(18.dp),
            color = containerColor,
            shadowElevation = shadowElevation,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),

            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .heightIn(min = 54.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .scale(iconScale)
                            .background(iconBackgroundColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused },
                        singleLine = true,
                        keyboardOptions = keyboardOptions,
                        visualTransformation = visualTransformation,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        cursorBrush = SolidColor(Color.White),
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
                                            color = Color.White.copy(alpha = 0.46f),
                                        ),
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }
            }
        }
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
