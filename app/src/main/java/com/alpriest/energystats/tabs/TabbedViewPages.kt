package com.alpriest.energystats.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import com.alpriest.energystats.ui.flow.PowerFlowTabView
import com.alpriest.energystats.ui.paramsgraph.NavigableParametersGraphTabView
import com.alpriest.energystats.ui.settings.NavigableSettingsView
import com.alpriest.energystats.ui.statsgraph.StatsTabView
import com.alpriest.energystats.ui.summary.SummaryView

@Composable
fun TabbedViewPages(page: Int, dependencies: TabbedViewDependencies, topBarSettings: MutableState<TopBarSettings>, navController: NavHostController) {
    when (page) {
        0 -> PowerFlowTabView(
            topBarSettings,
            dependencies.network,
            dependencies.configManager,
            dependencies.userManager,
            dependencies.themeStream,
            dependencies.widgetDataSharer,
            dependencies.bannerAlertManager,
            dependencies.templateStore
        ).Content(themeStream = dependencies.themeStream)

        1 -> StatsTabView(
            topBarSettings,
            dependencies.configManager,
            dependencies.network,
            dependencies.onWriteTempFile,
            dependencies.filePathChooser,
            dependencies.themeStream,
            dependencies.userManager
        ).Content()

        2 -> NavigableParametersGraphTabView(
            topBarSettings,
            navController,
            dependencies.configManager,
            dependencies.userManager,
            dependencies.network,
            dependencies.onWriteTempFile,
            dependencies.filePathChooser,
            dependencies.themeStream,
            dependencies.solarForecastingProvider
        ).Content()

        3 -> SummaryView(
            dependencies.configManager,
            dependencies.userManager,
            dependencies.network,
            dependencies.solarForecastingProvider
        ).NavigableContent(topBarSettings, navController, themeStream = dependencies.themeStream)

        4 -> NavigableSettingsView(
            topBarSettings,
            navController,
            dependencies.configManager,
            dependencies.userManager,
            dependencies.onLogout,
            dependencies.onRateApp,
            dependencies.onBuyMeCoffee,
            dependencies.network,
            dependencies.solarForecastingProvider,
            dependencies.templateStore
        )
    }
}