package com.example.stormpilot.features.auth.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.R
import com.example.stormpilot.ui.theme.extendedColors

@Composable
internal fun AuthOptions(
    switchPrompt: String,
    switchActionLabel: String,
    onSwitchMode: () -> Unit,
) {
    val colors = MaterialTheme.extendedColors
    Text(
        text = buildAnnotatedString {
            append(switchPrompt)
            append(' ')
            withLink(
                LinkAnnotation.Clickable(
                    tag = "switch_auth_mode",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = colors.theme.vibrantPrimary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ),
                    linkInteractionListener = { onSwitchMode() },
                ),
            ) {
                append(switchActionLabel)
            }
        },
        color = colors.theme.tonalOutlineVariant,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(20.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        HorizontalDivider(modifier = Modifier.width(14.dp), color = colors.auth.supportingContent)
        Text(
            text = "Or Sign in With",
            color = colors.theme.tonalOutlineVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(modifier = Modifier.width(14.dp), color = colors.auth.supportingContent)
    }

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AuthProviderButton("Apple", R.drawable.apple_logo_white, Modifier.weight(1f))
        AuthProviderButton("Google", R.drawable.google_logo, Modifier.weight(1f))
    }
}

@Composable
private fun AuthProviderButton(label: String, @DrawableRes logo: Int, modifier: Modifier) {
    val colors = MaterialTheme.extendedColors.auth
    Button(
        onClick = {},
        modifier = modifier.heightIn(min = 48.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.unfocusedInputContainer,
            contentColor = colors.primaryContent,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Image(
            painter = painterResource(logo),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun SignUpLegalText() {
    val colors = MaterialTheme.extendedColors
    val linkStyle = SpanStyle(color = colors.theme.vibrantPrimary)
    Text(
        text = buildAnnotatedString {
            append("By creating an account, you agree to our ")
            withStyle(linkStyle) { append("Privacy Policy") }
            append(" and ")
            withStyle(linkStyle) { append("Terms & Conditions") }
        },
        color = colors.theme.tonalOutlineVariant,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
    )
}
