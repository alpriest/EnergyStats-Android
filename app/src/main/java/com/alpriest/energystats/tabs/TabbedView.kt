package com.alpriest.energystats.tabs

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeConfigStore
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.stores.WidgetDataSharer
import com.alpriest.energystats.ui.flow.BannerAlertManager
import com.alpriest.energystats.ui.login.ConfigManager
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStore
import com.alpriest.energystats.ui.summary.DemoSolarForecasting
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabbedView(dependencies: TabbedViewDependencies) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val titles = listOf(
        TitleItem(stringResource(R.string.power_flow_tab), Icons.Default.SwapVert, false),
        TitleItem(stringResource(R.string.stats_tab), Icons.Default.BarChart, false),
        TitleItem(stringResource(R.string.parameters), Icons.Default.Insights, false),
        TitleItem(stringResource(R.string.summary_tab), Icons.AutoMirrored.Filled.MenuBook, false),
        TitleItem(stringResource(R.string.settings_tab), Icons.Default.Settings, true)
    )
    val pagerState = rememberPagerState(pageCount = { titles.size } )
    val topBarSettings = remember { mutableStateOf(TopBarSettings(false, "", {}, null)) }

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
                        topBarSettings.value.backButtonAction?.let {
                            IconButton(onClick = {
                                it()
                            }) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, "backIcon")
                            }
                        }
                    },
                    title = {
                        topBarSettings.value.title?.let {
                            Text(it)
                        }
                    },
                    actions = topBarSettings.value.actions
                )
            }
        },
        contentWindowInsets = WindowInsets.Companion.navigationBars,
        content = { padding ->
            HorizontalPager(
                modifier = Modifier.Companion.padding(padding),
                state = pagerState,
                userScrollEnabled = false
            ) { page ->
                TabbedViewPages(page, dependencies, topBarSettings)
            }
        },
        bottomBar = {
            TabbedViewBottomBar(pagerState, titles, dependencies)
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
