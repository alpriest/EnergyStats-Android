package com.alpriest.energystats

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
                is LoggedIn ->
                    LoadedView(appContainer)

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

@Composable
fun LoadedView(appContainer: AppContainer) {
    val loadingStateFlow = rememberSaveable { mutableStateOf(true) }
    val isLoading = loadingStateFlow.value
    val loginState = appContainer.userManager.loggedInState.collectAsState()

    LaunchedEffect(null) {
        if (loginState.value.loadState == LoggedIn) {
            try {
                appContainer.configManager.fetchDevices()
                appContainer.configManager.refreshFirmwareVersion()
            } finally {
                loadingStateFlow.value = false
            }
        }
    }

    if (isLoading) {
        LoadingView(title = stringResource(R.string.loading))
    } else {
        TabbedView(
            configManager = appContainer.configManager,
            network = appContainer.networking,
            userManager = appContainer.userManager,
            { appContainer.userManager.logout() },
            themeStream = appContainer.configManager.themeStream,
            networkStore = appContainer.networkStore,
            { appContainer.openAppInPlayStore() },
            { appContainer.openUrl(it) },
            { appContainer.buyMeACoffee() },
            { baseFilename, content -> appContainer.writeToTempFile(baseFilename, content) }
        )
    }
}