package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun HomeIconView(viewModel: LoadedPowerFlowViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier, iconHeight: Dp) {
    val showHomeTotal = themeStream.collectAsState().value.showHomeTotal
    val fontSize = themeStream.collectAsState().value.fontSize()
    val smallFontSize = themeStream.collectAsState().value.smallFontSize()
    val decimalPlaces = themeStream.collectAsState().value.decimalPlaces
    val displayUnit = themeStream.collectAsState().value.displayUnit
    val context = LocalContext.current
    val homeTotal = viewModel.homeTotal.collectAsState().value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        HouseView(
            modifier = Modifier.height(iconHeight).width(iconHeight * 1.1f),
            themeStream
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