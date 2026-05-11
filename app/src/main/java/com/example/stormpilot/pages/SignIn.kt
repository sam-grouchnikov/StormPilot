package com.example.stormpilot.pages

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.StormPilotTheme
import kotlinx.coroutines.delay

@Composable
fun SignIn(onLogin: () -> Unit) {
    val isDarkMode = remember {mutableStateOf(true)}
    val contentVisible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(70)
        contentVisible.value = true
    }

    StormPilotTheme(
        darkTheme = isDarkMode.value,
        dynamicColor = false,
        useSurfaceContainerNavigationBar = true
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 30.dp, bottom = 80.dp, end = 30.dp),

                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome back!",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.signInEntrance(contentVisible.value, 0)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Log in to start chasing!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.signInEntrance(contentVisible.value, 1)
                )
                Spacer(modifier = Modifier.height(50.dp))
                Text(
                    text = "Email",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.W600,
                    fontSize = 17.sp,
                    modifier = Modifier.signInEntrance(contentVisible.value, 2)
                )
                Spacer(modifier = Modifier.height(8.dp))
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                LinedIconInputField(
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Outlined.Email,
                    placeholder = "example@domain.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.signInEntrance(contentVisible.value, 3)
                )
                Spacer(modifier = Modifier.height(25.dp))
                Text(
                    text = "Password",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.W600,
                    fontSize = 17.sp,
                    modifier = Modifier.signInEntrance(contentVisible.value, 4)
                )
                Spacer(modifier = Modifier.height(15.dp))
                LinedIconInputField(
                    value = password,
                    onValueChange = { password = it },
                    icon = Icons.Outlined.Lock,
                    placeholder = "123",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.signInEntrance(contentVisible.value, 5)
                )
                Spacer(modifier = Modifier.height(45.dp))
                Button(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth(1.0f)
                        .height(50.dp)
                        .signInEntrance(contentVisible.value, 6), // Button takes 80% width
                    shape = RoundedCornerShape(35.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Log In",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.signInEntrance(visible: Boolean, index: Int): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(420, delayMillis = index * 55, easing = FastOutSlowInEasing),
        label = "signin_alpha_$index"
    )
    val y by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = tween(460, delayMillis = index * 55, easing = FastOutSlowInEasing),
        label = "signin_y_$index"
    )

    return graphicsLayer {
        this.alpha = alpha
        translationY = y
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
    startPadding: Int = 7, // keep your style default
) {
    Column(modifier = modifier.fillMaxWidth()) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startPadding.dp)
                .heightIn(min = 24.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (placeholder != null && value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
