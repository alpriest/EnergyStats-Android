package com.alpriest.energystats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.RawDataStore
import com.alpriest.energystats.models.RawDataStoring
import com.alpriest.energystats.preview.FakeConfigStore
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.PowerFlowTabView
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.graph.StatsGraphTabView
import com.alpriest.energystats.ui.graph.StatsGraphTabViewModel
import com.alpriest.energystats.ui.login.ConfigManager
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.SettingsView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeView(
    configManager: ConfigManaging,
    network: Networking,
    userManager: UserManaging,
    onLogout: () -> Unit,
    themeStream: MutableStateFlow<AppTheme>,
    rawDataStore: RawDataStoring,
    onRateApp: () -> Unit,
    onSendUsEmail: () -> Unit,
    onBuyMeCoffee: () -> Unit
) {
    val state = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val titles = listOf(
        "Power flow" to Icons.Default.SwapVert,
        "Stats" to Icons.Default.BarChart
    )

    Scaffold(
        scaffoldState = state,
        topBar = {
            TopAppBar(
                title = { Text("Energy Stats") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch { if (state.drawerState.isClosed) state.drawerState.open() else state.drawerState.close() }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Menu, "Menu"
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.surface
            )
        },
        content = { padding ->
            HorizontalPager(
                modifier = Modifier.padding(bottom = 36.dp),
                count = titles.size,
                state = pagerState
            ) { page ->
                when (page) {
                    0 -> PowerFlowTabView(network, configManager, rawDataStore).Content(themeStream = themeStream)
                    1 -> StatsGraphTabView(StatsGraphTabViewModel(configManager, network))
                }
            }
        },
        drawerBackgroundColor = MaterialTheme.colors.background,
        drawerContent = {
            SettingsView(
                config = configManager,
                userManager = userManager,
                onLogout = onLogout,
                rawDataStore = rawDataStore,
                onRateApp = onRateApp,
                onSendUsEmail = onSendUsEmail,
                onBuyMeCoffee = onBuyMeCoffee
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    backgroundColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier
                                .pagerTabIndicatorOffset(pagerState, tabPositions),
                            color = Color.Black,
                        )
                    }
                ) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.scrollToPage(
                                        index
                                    )
                                }
                            },
                            content = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Icon(imageVector = title.second, contentDescription = null)
                                    Text(
                                        text = title.first
                                    )
                                }
                            },
                            selectedContentColor = colors.primary,
                            unselectedContentColor = DimmedTextColor,
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomepagePreview() {
    EnergyStatsTheme(darkTheme = true) {
        HomeView(
            ConfigManager(
                config = FakeConfigStore(),
                networking = DemoNetworking(),
                rawDataStore = RawDataStore(),
                appVersion = "1.19"
            ),
            network = DemoNetworking(),
            userManager = FakeUserManager(),
            {},
            themeStream = MutableStateFlow(AppTheme.preview()),
            rawDataStore = RawDataStore(),
            {},
            {},
            {}
        )
    }
}
