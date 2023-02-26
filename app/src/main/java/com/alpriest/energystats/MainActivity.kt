package com.alpriest.energystats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.ui.login.CredentialsView
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.login.*
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as EnergyStatsApplication).appContainer

        setContent {
            val systemUiController = rememberSystemUiController()
            if (isSystemInDarkTheme()) {
                SideEffect {
                    systemUiController.setSystemBarsColor(Color.Black, darkIcons = false)
                }
            }
            val theme = appContainer.configManager.themeStream.collectAsState()

            EnergyStatsTheme(useLargeDisplay = theme.value == AppTheme.UseLargeDisplay) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val loginState = appContainer.userManager.loggedInState.collectAsState().value
                    val coroutineScope = rememberCoroutineScope()

                    when (loginState.loadState) {
                        is LoggedIn ->
                            HomeView(
                                configManager = appContainer.configManager,
                                network = appContainer.networking,
                                userManager = appContainer.userManager,
                                { appContainer.userManager.logout() },
                                themeStream = appContainer.configManager.themeStream,
                                rawDataStore = appContainer.rawDataStore
                            )
                        is LoggedOut ->
                            CredentialsView(
                                errorMessage = loginState.loadState.reason,
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
                            LoadingView(title = "Logging in...")
                    }
                }
            }
        }
    }
}
