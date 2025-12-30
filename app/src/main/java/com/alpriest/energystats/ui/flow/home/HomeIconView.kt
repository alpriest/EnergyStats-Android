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
import com.alpriest.energystats.shared.helpers.energy
import com.alpriest.energystats.shared.ui.HouseView
import com.alpriest.energystats.ui.flow.battery.iconBackgroundColor
import com.alpriest.energystats.ui.flow.battery.iconForegroundColor
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun HomeIconView(viewModel: LoadedPowerFlowViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier, iconHeight: Dp) {
    val showHomeTotal = themeStream.collectAsStateWithLifecycle().value.showHomeTotal
    val fontSize = themeStream.collectAsStateWithLifecycle().value.fontSize()
    val smallFontSize = themeStream.collectAsStateWithLifecycle().value.smallFontSize()
    val displayUnit = themeStream.collectAsStateWithLifecycle().value.displayUnit
    val context = LocalContext.current
    val homeTotal = viewModel.homeTotal.collectAsStateWithLifecycle().value
    val foregroundColor = iconForegroundColor(isDarkMode(themeStream))
    val backgroundColor = iconBackgroundColor(isDarkMode(themeStream))

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