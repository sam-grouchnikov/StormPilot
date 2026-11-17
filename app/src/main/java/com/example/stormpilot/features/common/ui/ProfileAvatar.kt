package com.example.stormpilot.features.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.ui.theme.extendedColors

@Composable
fun ProfileAvatar(
    modifier: Modifier = Modifier,
    circleSize: Int,
    textSize: Int = 15,
    userName: String = "Sam",
    outlineColor: Color? = null,
    onClick: () -> Unit,
) {
    val avatarLetter = userName.firstOrNull()?.uppercase() ?: "?"

    val colors = MaterialTheme.extendedColors
    val outlineWidth = if (outlineColor == null) 2.dp else 3.dp
    Box(
        modifier = modifier
            .size(circleSize.dp)
            .clip(CircleShape)
            .border(outlineWidth, outlineColor ?: MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(if (outlineColor == null) 0.dp else outlineWidth + 2.5.dp)
            .background(colors.theme.vibrantAccent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = avatarLetter,
            fontSize = textSize.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun AccountMenuAnchor(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    circleSize: Int = 43,
    textSize: Int = 15,
    outlineColor: Color? = MaterialTheme.extendedColors.theme.avatarOutline,
) {
    ProfileAvatar(
        modifier = modifier,
        circleSize = circleSize,
        textSize = textSize,
        outlineColor = outlineColor,
        onClick = onClick,
    )
}
