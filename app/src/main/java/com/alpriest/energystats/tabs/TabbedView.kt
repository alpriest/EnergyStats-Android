package com.alpriest.energystats.tabs

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeConfigStore
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.stores.WidgetDataSharer
import com.alpriest.energystats.ui.flow.BannerAlertManager
import com.alpriest.energystats.ui.login.ConfigManager
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.darkenedBackground
import com.alpriest.energystats.ui.settings.darkenedBackgroundColor
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStore
import com.alpriest.energystats.ui.summary.DemoSolarForecasting
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
fun TabbedView(dependencies: TabbedViewDependencies) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val titles = listOf(
        TitleItem(stringResource(R.string.power_flow_tab), Icons.Default.SwapVert, false),
        TitleItem(stringResource(R.string.stats_tab), Icons.Default.BarChart, false),
        TitleItem(stringResource(R.string.parameters), Icons.Default.Insights, false),
        TitleItem(stringResource(R.string.summary_tab), Icons.AutoMirrored.Filled.MenuBook, false),
        TitleItem(stringResource(R.string.settings_tab), Icons.Default.Settings, true)
    )
    val navController = rememberNavController()
    val topBarSettings = remember { mutableStateOf(TopBarSettings(false, false, "", {})) }

    LaunchedEffect(dependencies.configManager.lastSettingsResetTime) {
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
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
        contentWindowInsets = WindowInsets.Companion.navigationBars,
        content = { padding ->
            HorizontalPager(
                modifier = Modifier.Companion.padding(padding),
                count = titles.size,
                state = pagerState,
                userScrollEnabled = false
            ) { page ->
                TabbedViewPages(page, dependencies, topBarSettings, navController)
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .darkenedBackground()
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = darkenedBackgroundColor(),
                    divider = {},
                    modifier = Modifier.Companion.windowInsetsPadding(WindowInsets.Companion.navigationBars)
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
                                Box(contentAlignment = Alignment.Companion.TopEnd) {
                                    Column(
                                        horizontalAlignment = Alignment.Companion.CenterHorizontally,
                                        modifier = Modifier.Companion.padding(vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = item.icon, contentDescription = null)
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    if (dependencies.configManager.isDemoUser && item.isSettings) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.Companion.Red),
                                            shape = MaterialTheme.shapes.medium,
                                            modifier = Modifier.Companion
                                                .padding(2.dp)
                                                .offset(x = 16.dp, y = 2.dp)
                                        ) {
                                            Text(
                                                "Demo",
                                                color = Color.Companion.White,
                                                fontWeight = FontWeight.Companion.Bold,
                                                modifier = Modifier.Companion
                                                    .padding(horizontal = 2.dp),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = DimmedTextColor,
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TabbedViewPreview() {
    val themeStream = MutableStateFlow(AppTheme.Companion.demo())
    val dependencies = TabbedViewDependencies(
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

    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        TabbedView(dependencies = dependencies)
    }
}
