package com.alpriest.energystats.tabs

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import com.alpriest.energystats.ui.flow.PowerFlowTabView
import com.alpriest.energystats.ui.paramsgraph.NavigableParametersGraphTabView
import com.alpriest.energystats.ui.settings.NavigableSettingsView
import com.alpriest.energystats.ui.statsgraph.NavigableStatsGraphTabView
import com.alpriest.energystats.ui.summary.SummaryView

@Composable
fun TabbedViewPages(page: Int, dependencies: TabbedViewDependencies, topBarSettings: MutableState<TopBarSettings>) {
    ConfigureStatusBarColours(page, dependencies.appSettingsStream)
    val context = LocalContext.current
    val application = context.applicationContext as Application

    when (page) {
        0 -> PowerFlowTabView(
            application,
            topBarSettings,
            dependencies.network,
            dependencies.configManager,
            dependencies.userManager,
            dependencies.appSettingsStream,
            dependencies.widgetDataSharer,
            dependencies.bannerAlertManager,
            dependencies.templateStore,
            dependencies.apiKeyProvider
        ).Content(appSettingsStream = dependencies.appSettingsStream)

        1 -> NavigableStatsGraphTabView(
            application,
            topBarSettings,
            dependencies.configManager,
            dependencies.network,
            dependencies.onWriteTempFile,
            dependencies.filePathChooser,
            dependencies.appSettingsStream,
            dependencies.userManager
        ).Content()

        2 -> NavigableParametersGraphTabView(
            topBarSettings,
            dependencies.configManager,
            dependencies.userManager,
            dependencies.network,
            dependencies.onWriteTempFile,
            dependencies.filePathChooser,
            dependencies.appSettingsStream,
            dependencies.solarForecastingProvider
        ).Content()

        3 -> SummaryView(
            dependencies.configManager,
            dependencies.userManager,
            dependencies.network,
            dependencies.solarForecastingProvider
        ).NavigableContent(topBarSettings, appSettingsStream = dependencies.appSettingsStream)

        4 -> NavigableSettingsView(
            topBarSettings,
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