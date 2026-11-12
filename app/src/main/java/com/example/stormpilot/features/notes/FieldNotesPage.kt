package com.example.stormpilot.features.notes


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.features.common.ui.AccountMenuAnchor
import com.example.stormpilot.features.dashboard.ui.DashboardOverviewScreen
import com.example.stormpilot.features.dashboard.ui.TopIconRow
import com.example.stormpilot.features.shared.viewmodels.AlertsViewModel
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.ui.theme.extendedColors

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FieldNotesPage() {
    val colors = MaterialTheme.extendedColors

    StormPilotTheme(
        opaqueNavigationBar = true,
        navigationBarColorOverride = colors.navigation.barSurface
    ) {

        Text("Hello")
    }
}
