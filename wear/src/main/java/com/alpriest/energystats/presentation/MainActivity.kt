package com.alpriest.energystats.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.alpriest.energystats.presentation.theme.EnergyStatsTheme
import com.alpriest.energystats.shared.models.LoadState

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            EnergyStatsTheme {
                val vm: WearHomeViewModel = viewModel()
                val state by vm.state.collectAsState()
                val loadState = state.state

                val pagerState = rememberPagerState(initialPage = 0) { 6 }

                when (loadState) {
                    is LoadState.Inactive -> Tabs(pagerState, state)
                    is LoadState.Error -> MessageView(loadState.reason)
                    is LoadState.Active.Loading -> MessageView(stringResource(loadState.titleResId))
                    else ->
                        Text("")
                }
            }
        }
    }

    @Composable
    fun Tabs(pagerState: PagerState, state: WearPowerFlowState) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                        totalExport = state.totalExport,
                        totalImport = state.totalImport
                    )

                    1 -> SolarPowerView(
                        IconScale.LARGE,
                        solarAmount = state.solarAmount,
                        solarRangeDefinitions = state.solarRangeDefinitions
                    )

                    2 -> GridPowerView(
                        IconScale.LARGE,
                        amount = state.gridAmount,
                        totalExport = state.totalExport,
                        totalImport = state.totalImport
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

                    5 -> LastUpdatedView(state.lastUpdated)
                }
            }
            PagerIndicator(
                pageCount = 6,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }
    }

    @Composable
    fun PagerIndicator(
        pageCount: Int,
        currentPage: Int,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 6.dp else 4.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage)
                                MaterialTheme.colors.primary
                            else
                                MaterialTheme.colors.onBackground.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

