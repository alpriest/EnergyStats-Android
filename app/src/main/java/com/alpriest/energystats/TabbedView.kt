package com.alpriest.energystats

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.preview.FakeConfigStore
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoFoxESSNetworking
import com.alpriest.energystats.services.InMemoryLoggingNetworkStore
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.stores.SharedPreferencesCredentialStore
import com.alpriest.energystats.ui.flow.PowerFlowTabView
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.login.ConfigManager
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.NavigableParametersGraphTabView
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.NavigableSettingsView
import com.alpriest.energystats.ui.settings.solcast.SolarForecasting
import com.alpriest.energystats.ui.statsgraph.StatsTabView
import com.alpriest.energystats.ui.statsgraph.StatsTabViewModel
import com.alpriest.energystats.ui.summary.DemoSolarForecasting
import com.alpriest.energystats.ui.summary.SummaryView
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
fun TabbedView(
    configManager: ConfigManaging,
    network: FoxESSNetworking,
    userManager: UserManaging,
    onLogout: () -> Unit,
    themeStream: MutableStateFlow<AppTheme>,
    networkStore: InMemoryLoggingNetworkStore,
    onRateApp: () -> Unit,
    onBuyMeCoffee: () -> Unit,
    onWriteTempFile: (String, String) -> Uri?,
    filePathChooser: (filename: String, action: (Uri) -> Unit) -> Unit?,
    credentialStore: CredentialStore,
    solarForecastingProvider: () -> SolarForecasting
) {
    val state = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val titles = listOf(
        TitleItem(stringResource(R.string.power_flow_tab), Icons.Default.SwapVert, false),
        TitleItem(stringResource(R.string.stats_tab), Icons.Default.BarChart, false),
        TitleItem("Parameters", Icons.Default.Insights, false),
        TitleItem("Summary", Icons.Default.MenuBook, false),
        TitleItem(stringResource(R.string.settings_tab), Icons.Default.Settings, true)
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
                    0 -> PowerFlowTabView(network, configManager, userManager, themeStream).Content(themeStream = themeStream)
                    1 -> StatsTabView(StatsTabViewModel(configManager, network, onWriteTempFile), filePathChooser, themeStream)
                    2 -> NavigableParametersGraphTabView(configManager, network, onWriteTempFile, filePathChooser, themeStream).Content()
                    3 -> SummaryView(configManager, network, solarForecastingProvider).Content(themeStream = themeStream)
                    4 -> NavigableSettingsView(
                        config = configManager,
                        userManager = userManager,
                        onLogout = onLogout,
                        network = network,
                        networkStore = networkStore,
                        onRateApp = onRateApp,
                        onBuyMeCoffee = onBuyMeCoffee,
                        credentialStore = credentialStore,
                        solarForecastingProvider = solarForecastingProvider
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
                                                modifier = Modifier
                                                    .padding(horizontal = 2.dp)
                                                    .padding(bottom = 2.dp),
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
    val themeStream = MutableStateFlow(AppTheme.preview())
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        TabbedView(
            ConfigManager(
                config = FakeConfigStore(),
                networking = DemoFoxESSNetworking(),
                appVersion = "1.19",
                themeStream = themeStream
            ),
            network = DemoFoxESSNetworking(),
            userManager = FakeUserManager(),
            {},
            themeStream = themeStream,
            networkStore = InMemoryLoggingNetworkStore(),
            {},
            {},
            { _, _ -> null },
            { _, _ -> },
            SharedPreferencesCredentialStore(LocalContext.current.getSharedPreferences("com.alpriest.energystats", Context.MODE_PRIVATE)),
            { DemoSolarForecasting() }
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
