package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.House
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Wh
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.ui.flow.battery.iconBackgroundColor
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun HomeIconView(viewModel: HomePowerFlowViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier, iconHeight: Dp) {
    val showHomeTotal = themeStream.collectAsState().value.showHomeTotal
    val fontSize = themeStream.collectAsState().value.fontSize()
    val decimalPlaces = themeStream.collectAsState().value.decimalPlaces
    val showValuesInWatts = themeStream.collectAsState().value.showValuesInWatts
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        HouseView(
            modifier = Modifier.size(iconHeight)
        )

        if (showHomeTotal) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (showValuesInWatts) viewModel.homeTotal.Wh(decimalPlaces) else viewModel.homeTotal.kWh(decimalPlaces),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    context.getString(R.string.used_today),
                    fontSize = fontSize,
                    color = Color.Gray,
                )
            }
        }
    }
}