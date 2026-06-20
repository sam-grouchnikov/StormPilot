package com.example.stormpilot.features.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.R
import com.example.stormpilot.ui.theme.ExtendedColors
import com.example.stormpilot.ui.theme.StormPilotTheme

@Composable
fun WelcomePage(onSignInClick: () -> Unit, onSignUpClick: () -> Unit) {
    StormPilotTheme(
        darkTheme = true,
        dynamicColor = false,
        opaqueNavigationBar = false,
        useSurfaceContainerNavigationBar = true,
        syncAppDarkMode = false,
    ) {
        val colors = ExtendedColors()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.welcomeNavyStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colors.welcomeNavyStart,
                                colors.welcomeNavyEnd,
                            ),
                        ),
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "StormPilot logo",
                        modifier = Modifier.size(138.dp),
                        contentScale = ContentScale.Fit,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "StormPilot",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 10.sp,
                            fontSize = 50.sp
                        ),
                        color = Color.White,
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Weather-aware routing for the road ahead.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = Color.White.copy(alpha = 0.74f),
                    )

                    Spacer(modifier = Modifier.height(94.dp))

                    Button(
                        onClick = onSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = colors.welcomeNavyStart,
                        ),
                    ) {
                        Text(
                            text = "Log in",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onSignUpClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.44f),
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = "Create new account",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }
            }
        }
    }
}
