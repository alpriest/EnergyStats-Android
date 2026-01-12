package com.alpriest.energystats

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpriest.energystats.tabs.TabbedView
import com.alpriest.energystats.tabs.TabbedViewDependencies
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.login.LoggedIn
import com.alpriest.energystats.ui.login.LoggedOut
import com.alpriest.energystats.ui.login.LoggingIn
import com.alpriest.energystats.ui.login.WelcomeView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun MainAppView(appContainer: AppContainer) {
    val theme = appContainer.configManager.appSettingsStream.collectAsStateWithLifecycle()
    val loginState = appContainer.userManager.loggedInState.collectAsStateWithLifecycle()
    var showingApiKey by remember { mutableStateOf(false) }
    val dependencies = remember {
        TabbedViewDependencies(
            configManager = appContainer.configManager,
            network = appContainer.networking,
            userManager = appContainer.userManager,
            { appContainer.userManager.logout() },
            themeStream = appContainer.configManager.appSettingsStream,
            { appContainer.openAppInPlayStore() },
            { appContainer.buyMeACoffee() },
            { baseFilename, content -> appContainer.writeToTempFile(baseFilename, content) },
            { filename, action -> appContainer.showFileChooser(filename, action) },
            solarForecastingProvider = appContainer.solarForecastingProvider,
            appContainer.widgetDataSharer,
            appContainer.bannerAlertManager,
            appContainer.templateStore,
            { appContainer.credentialStore.getApiKey() }
        )
    }

    EnergyStatsTheme(useLargeDisplay = theme.value.useLargeDisplay, colorThemeMode = theme.value.colorTheme) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            val loginStateValue = loginState.value

            when (loginStateValue.loadState) {
                is LoggedIn -> TabbedView(dependencies)

                is LoggedOut ->
                    WelcomeView(
                        showingApiKey,
                        appContainer.userManager,
                        themeStream = appContainer.configManager.appSettingsStream
                    ) { showingApiKey = !showingApiKey }

                is LoggingIn -> LoadingView(stringResource(R.string.logging_in), stringResource(R.string.still_logging_in))
            }
        }
    }
}
