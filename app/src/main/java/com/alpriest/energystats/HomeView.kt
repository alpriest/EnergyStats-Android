package com.alpriest.energystats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class TitleItem(
    val title: String,
    val icon: ImageVector,
    val isSettings: Boolean
)

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
        TitleItem("Power flow", Icons.Default.SwapVert, false),
        TitleItem("Stats", Icons.Default.BarChart, false),
        TitleItem("Settings", Icons.Default.Settings, true)
    )

    Scaffold(
        scaffoldState = state,
        content = { padding ->
            HorizontalPager(
                modifier = Modifier.padding(bottom = 54.dp),
                count = titles.size,
                state = pagerState
            ) { page ->
                when (page) {
                    0 -> PowerFlowTabView(network, configManager, rawDataStore).Content(themeStream = themeStream)
                    1 -> StatsGraphTabView(StatsGraphTabViewModel(configManager, network))
                    2 -> SettingsView(
                        config = configManager,
                        userManager = userManager,
                        onLogout = onLogout,
                        rawDataStore = rawDataStore,
                        onRateApp = onRateApp,
                        onSendUsEmail = onSendUsEmail,
                        onBuyMeCoffee = onBuyMeCoffee
                    )
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    backgroundColor = darkenColor(colors.background, 0.02f),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier
                                .pagerTabIndicatorOffset(pagerState, tabPositions),
                            color = Color.Black,
                        )
                    }
                ) {
                    titles.forEachIndexed { index, item ->
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
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = item.icon, contentDescription = null)
                                        Text(
                                            text = item.title,
                                            fontSize = 10.sp
                                        )

                                    }

                                    if (configManager.isDemoUser && item.isSettings) {
                                        Card(
                                            backgroundColor = Color.Red,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .padding(2.dp)
                                                .offset(x = 16.dp, y = 2.dp)
                                        ) {
                                            Text(
                                                "Demo",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 2.dp).padding(bottom = 2.dp),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
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

fun darkenColor(color: Color, percentage: Float): Color {
    val argb = color.toArgb()
    val alpha = argb ushr 24
    val red = argb shr 16 and 0xFF
    val green = argb shr 8 and 0xFF
    val blue = argb and 0xFF

    val darkenedRed = (red * (1 - percentage)).toInt()
    val darkenedGreen = (green * (1 - percentage)).toInt()
    val darkenedBlue = (blue * (1 - percentage)).toInt()

    return Color(alpha = alpha, red = darkenedRed, green = darkenedGreen, blue = darkenedBlue)
}
