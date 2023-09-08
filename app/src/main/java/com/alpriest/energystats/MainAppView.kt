package com.alpriest.energystats

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.login.CredentialsView
import com.alpriest.energystats.ui.login.LoggedIn
import com.alpriest.energystats.ui.login.LoggedOut
import com.alpriest.energystats.ui.login.LoggingIn
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.launch

@Composable
fun MainAppView(appContainer: AppContainer) {
    val theme = appContainer.configManager.themeStream.collectAsState()
    val loginState = appContainer.userManager.loggedInState.collectAsState()

    EnergyStatsTheme(useLargeDisplay = theme.value.useLargeDisplay) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            val coroutineScope = rememberCoroutineScope()
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
                        { baseFilename, content -> appContainer.writeToTempFile(baseFilename, content) }
                    )
                }

                is LoggedOut ->
                    CredentialsView(
                        errorMessage = loginStateValue.loadState.reason,
                        onLogin = { username, password ->
                            coroutineScope.launch {
                                appContainer.userManager.login(
                                    username,
                                    password
                                )
                            }
                        },
                        onDemoLogin = {
                            coroutineScope.launch {
                                appContainer.userManager.loginDemo()
                            }
                        }
                    )

                is LoggingIn ->
                    LoadingView(title = stringResource(R.string.logging_in))
            }
        }
    }
}

