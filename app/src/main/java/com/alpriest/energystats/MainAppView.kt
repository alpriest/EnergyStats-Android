package com.alpriest.energystats

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.UpgradeRequiredView
import com.alpriest.energystats.ui.login.APIKeyLoginView
import com.alpriest.energystats.ui.login.LoggedIn
import com.alpriest.energystats.ui.login.LoggedOut
import com.alpriest.energystats.ui.login.LoggingIn
import com.alpriest.energystats.ui.login.RequiresUpgrade
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun MainAppView(appContainer: AppContainer) {
    val theme = appContainer.configManager.themeStream.collectAsState()
    val loginState = appContainer.userManager.loggedInState.collectAsState()

    EnergyStatsTheme(useLargeDisplay = theme.value.useLargeDisplay, colorThemeMode = theme.value.colorTheme) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            val loginStateValue = loginState.value

            when (loginStateValue.loadState) {
                is LoggedIn -> {
                    TabbedView(
                        configManager = appContainer.configManager,
                        network = appContainer.networking,
                        userManager = appContainer.userManager,
                        { appContainer.userManager.logout() },
                        themeStream = appContainer.configManager.themeStream,
                        networkStore = appContainer.networkStore,
                        { appContainer.openAppInPlayStore() },
                        { appContainer.buyMeACoffee() },
                        { baseFilename, content -> appContainer.writeToTempFile(baseFilename, content) },
                        { filename, action -> appContainer.showFileChooser(filename, action) },
                        credentialStore = appContainer.credentialStore,
                        solarForecastingProvider = appContainer.solarForecastingProvider
                    )
                }

                is LoggedOut ->
                    APIKeyLoginView(appContainer.userManager).Content(themeStream = appContainer.configManager.themeStream)

                is LoggingIn ->
                    LoadingView(title = stringResource(R.string.logging_in))

                is RequiresUpgrade ->
                    UpgradeRequiredView(appContainer.userManager)
            }
        }
    }
}

