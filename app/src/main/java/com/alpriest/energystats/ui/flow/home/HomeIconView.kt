package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.ui.HouseView
import com.alpriest.energystats.shared.ui.iconBackgroundColor
import com.alpriest.energystats.shared.ui.iconForegroundColor
import com.alpriest.energystats.ui.flow.energy
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.isDarkMode
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeIconView(viewModel: LoadedPowerFlowViewModel, appSettingsStream: StateFlow<AppSettings>, modifier: Modifier, iconHeight: Dp) {
    val showHomeTotal = appSettingsStream.collectAsStateWithLifecycle().value.showHomeTotal
    val fontSize = appSettingsStream.collectAsStateWithLifecycle().value.fontSize()
    val smallFontSize = appSettingsStream.collectAsStateWithLifecycle().value.smallFontSize()
    val displayUnit = appSettingsStream.collectAsStateWithLifecycle().value.displayUnit
    val context = LocalContext.current
    val homeTotal = viewModel.homeTotal.collectAsStateWithLifecycle().value
    val foregroundColor = iconForegroundColor(isDarkMode(appSettingsStream))
    val backgroundColor = iconBackgroundColor(isDarkMode(appSettingsStream))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        HouseView(
            modifier = Modifier.height(iconHeight).width(iconHeight * 1.1f),
            foregroundColor,
            backgroundColor
        )

        if (showHomeTotal) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ShimmerText(
                    shimmering = homeTotal == null,
                    text = (homeTotal ?: 0.0).energy(displayUnit, 1),
                    fontSize = fontSize,
                )
                Text(
                    context.getString(R.string.used_today),
                    fontSize = smallFontSize,
                    color = Color.Gray,
                )
            }
        }
    }
}