package com.example.stormpilot.pages


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.rounded.Storm
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.StormPilotTheme


@Composable
fun WelcomePage(onSignInClick: () -> Unit, onSignUpClick: () -> Unit) {
    val isDarkMode = remember {mutableStateOf(true)}
    StormPilotTheme(darkTheme = isDarkMode.value, dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = 65.dp, top = 200.dp),
                // 1. Centers everything vertically
                verticalArrangement = Arrangement.Center,
                // 2. Centers everything horizontally
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Thunderstorm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(70.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "StormPilot",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 45.sp
                    )
                }

                // 3. Add larger Spacers for manual "breathing room"
                Spacer(modifier = Modifier.height(30.dp))

                // Part 2: Welcome Text
                Text(
                    text = "Your voice-first storm chasing hub",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.W500,
                    lineHeight = 38.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 63.dp)
                        .align(Alignment.Start)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onSignUpClick,
                    modifier = Modifier.fillMaxWidth(0.81f).height(50.dp), // Button takes 80% width
                    shape = RoundedCornerShape(35.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Text(
                        text = "Sign Up",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


                Spacer(modifier = Modifier.height(25.dp)) // Large gap before the button

                Button(
                    onClick = onSignInClick,
                    modifier = Modifier.fillMaxWidth(0.81f).height(50.dp), // Button takes 80% width
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