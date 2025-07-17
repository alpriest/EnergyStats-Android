package com.alpriest.energystats

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeConfigStore
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.WidgetDataSharer
import com.alpriest.energystats.stores.WidgetDataSharing
import com.alpriest.energystats.ui.flow.BannerAlertManager
import com.alpriest.energystats.ui.flow.BannerAlertManaging
import com.alpriest.energystats.ui.flow.PowerFlowTabView
import com.alpriest.energystats.ui.login.ConfigManager
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.NavigableParametersGraphTabView
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.NavigableSettingsView
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStore
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStoring
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.statsgraph.StatsTabView
import com.alpriest.energystats.ui.summary.DemoSolarForecasting
import com.alpriest.energystats.ui.summary.SummaryView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class TitleItem(
    val title: String,
    val icon: ImageVector,
    val isSettings: Boolean
)

data class TopBarSettings(
    val topBarVisible: Boolean,
    val backButtonVisible: Boolean,
    val title: String,
    val actions: @Composable RowScope.() -> Unit
)

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TabbedView(
    configManager: ConfigManaging,
    network: Networking,
    userManager: UserManaging,
    onLogout: () -> Unit,
    themeStream: MutableStateFlow<AppTheme>,
    onRateApp: () -> Unit,
    onBuyMeCoffee: () -> Unit,
    onWriteTempFile: (String, String) -> Uri?,
    filePathChooser: (filename: String, action: (Uri) -> Unit) -> Unit?,
    solarForecastingProvider: () -> SolcastCaching,
    widgetDataSharer: WidgetDataSharing,
    bannerAlertManager: BannerAlertManaging,
    templateStore: TemplateStoring
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val titles = listOf(
        TitleItem(stringResource(R.string.power_flow_tab), Icons.Default.SwapVert, false),
        TitleItem(stringResource(R.string.stats_tab), Icons.Default.BarChart, false),
        TitleItem("Parameters", Icons.Default.Insights, false),
        TitleItem("Summary", Icons.AutoMirrored.Filled.MenuBook, false),
        TitleItem(stringResource(R.string.settings_tab), Icons.Default.Settings, true)
    )
    val navController = rememberNavController()
    val topBarSettings = remember { mutableStateOf(TopBarSettings(false, false, "", {})) }

    LaunchedEffect(configManager.lastSettingsResetTime) {
        scope.launch {
            pagerState.scrollToPage(0)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (topBarSettings.value.topBarVisible) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.primary,
                        titleContentColor = colorScheme.onPrimary,
                        navigationIconContentColor = colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        if (topBarSettings.value.backButtonVisible) {
                            IconButton(onClick = {
                                navController.popBackStack()
                            }) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, "backIcon")
                            }
                        }
                    },
                    title = {
                        Text(topBarSettings.value.title)
                    },
                    actions = topBarSettings.value.actions
                )
            }
        },
        contentWindowInsets = WindowInsets.navigationBars,
        content = { padding ->
            HorizontalPager(
                modifier = Modifier.padding(padding),
                count = titles.size,
                state = pagerState,
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> PowerFlowTabView(
                        topBarSettings,
                        network,
                        configManager,
                        userManager,
                        themeStream,
                        widgetDataSharer,
                        bannerAlertManager,
                        templateStore
                    ).Content(themeStream = themeStream)

                    1 -> StatsTabView(topBarSettings, configManager, network, onWriteTempFile, filePathChooser, themeStream, userManager).Content()
                    2 -> NavigableParametersGraphTabView(
                        topBarSettings,
                        navController,
                        configManager,
                        userManager,
                        network,
                        onWriteTempFile,
                        filePathChooser,
                        themeStream,
                        solarForecastingProvider
                    ).Content()

                    3 -> SummaryView(configManager, userManager, network, solarForecastingProvider).NavigableContent(topBarSettings, navController, themeStream = themeStream)
                    4 -> NavigableSettingsView(
                        topBarSettings,
                        navController,
                        configManager = configManager,
                        userManager = userManager,
                        onLogout = onLogout,
                        onRateApp = onRateApp,
                        onBuyMeCoffee = onBuyMeCoffee,
                        network = network,
                        solarForecastingProvider = solarForecastingProvider,
                        templateStore = templateStore
                    )
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .background(darkenColor(colorScheme.background, 0.04f))
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = darkenColor(colorScheme.background, 0.04f),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    contentColor = Color.Red
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
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    if (configManager.isDemoUser && item.isSettings) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.Red),
                                            shape = MaterialTheme.shapes.medium,
                                            modifier = Modifier
                                                .padding(2.dp)
                                                .offset(x = 16.dp, y = 2.dp)
                                        ) {
                                            Text(
                                                "Demo",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .padding(horizontal = 2.dp),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            },
                            selectedContentColor = colorScheme.primary,
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
    val themeStream = MutableStateFlow(AppTheme.demo())
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        TabbedView(
            ConfigManager(
                config = FakeConfigStore(),
                networking = DemoNetworking(),
                appVersion = "1.19",
                themeStream = themeStream
            ),
            network = DemoNetworking(),
            userManager = FakeUserManager(),
            {},
            themeStream = themeStream,
            {},
            {},
            { _, _ -> null },
            { _, _ -> },
            { DemoSolarForecasting() },
            WidgetDataSharer(FakeConfigStore()),
            BannerAlertManager(),
            TemplateStore(FakeConfigManager())
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
