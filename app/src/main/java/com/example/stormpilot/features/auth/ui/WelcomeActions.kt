package com.example.stormpilot.features.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stormpilot.ui.theme.extendedColors

@Composable
internal fun WelcomeActions(onSignInClick: () -> Unit, onSignUpClick: () -> Unit) {
    val colors = MaterialTheme.extendedColors
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.auth.primaryButtonContainer,
                contentColor = colors.auth.onPrimaryButtonContainer,
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
                .height(54.dp),
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(
                width = 1.dp,
                color = colors.auth.outlineButtonBorder,
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.auth.primaryContent,
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
