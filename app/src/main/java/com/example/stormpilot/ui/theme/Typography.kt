package com.example.stormpilot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

private val RobotoMono = GoogleFont("Manrope")

private val RobotoMonoFamily = FontFamily(
    Font(googleFont = RobotoMono, weight = FontWeight.Normal),
    Font(googleFont = RobotoMono, weight = FontWeight.Medium),
    Font(googleFont = RobotoMono, weight = FontWeight.SemiBold),
    Font(googleFont = RobotoMono, weight = FontWeight.Bold),
)

val StormPilotTypography = Typography(
    fontFamily = RobotoMonoFamily,
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 58.sp,
        lineHeight = 62.sp,
        letterSpacing = (-1.1).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 46.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.8).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    )
)
