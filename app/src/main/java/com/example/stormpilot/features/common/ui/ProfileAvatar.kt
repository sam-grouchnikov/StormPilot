package com.example.stormpilot.features.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import com.example.stormpilot.ui.theme.ExtendedColors

@Composable
fun ProfileAvatar(
    modifier: Modifier = Modifier,
    userName: String = "Sam",
    onClick: () -> Unit,
) {
    val avatarLetter = userName.firstOrNull()?.uppercase() ?: "?"

    val colors = ExtendedColors()
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .background(colors.profileIcon)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = avatarLetter,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun AccountMenuAnchor(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ProfileAvatar(
        modifier = modifier,
        onClick = onClick,
    )
}
