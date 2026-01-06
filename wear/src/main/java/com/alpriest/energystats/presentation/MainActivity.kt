package com.alpriest.energystats.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.presentation.theme.EnergyStatsTheme

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            EnergyStatsTheme {
                val vm: WearHomeViewModel = viewModel()
                val state by vm.state.collectAsState()

                val pagerState = rememberPagerState(initialPage = 0) { 5 }

                // Swipe left/right between pages
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> AllItemsView(
                            solarAmount = state.solarAmount,
                            houseLoadAmount = state.houseLoadAmount,
                            batteryChargeAmount = state.batterySOC,
                            batteryChargeLevel = state.batteryChargePower,
                            gridAmount = state.gridAmount,
                            solarRangeDefinitions = state.solarRangeDefinitions,
                        )

                        1 -> SolarPowerView(
                            IconScale.LARGE,
                            solarAmount = state.solarAmount,
                            solarRangeDefinitions = state.solarRangeDefinitions
                        )

                        2 -> GridPowerView(
                            IconScale.LARGE,
                            amount = state.gridAmount
                        )

                        3 -> BatteryPowerView(
                            IconScale.LARGE,
                            amount = state.batterySOC,
                            chargeLevel = state.batteryChargePower
                        )

                        4 -> HomePowerView(
                            IconScale.LARGE,
                            amount = state.houseLoadAmount
                        )
                    }
                }
            }
        }
    }
}
