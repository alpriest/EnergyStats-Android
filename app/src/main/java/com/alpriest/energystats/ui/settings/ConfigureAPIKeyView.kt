package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.login.HowToObtainAPIKeyView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ConfigureAPIKeyView(store: CredentialStore, navController: NavController, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier) {
    var apiKey by rememberSaveable { mutableStateOf(store.getApiKey() ?: "") }
    trackScreenView("API Key", "ConfigureAPIKeyView")

    ContentWithBottomButtonPair(
        navController,
        modifier = modifier,
        onConfirm = {
            store.store(apiKey)
            navController.popBackStack()
        },
        dirtyStateFlow = null,
        content = { innerModifier ->
            SettingsPage(innerModifier) {
                SettingsColumnWithChild {
                    OutlinedTextField(
                        modifier = Modifier.Companion.fillMaxWidth(),
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text(stringResource(R.string.api_key)) },
                        singleLine = true,
                        textStyle = TextStyle(MaterialTheme.colorScheme.onSecondary),
                    )

                    Text(
                        "If you need to change your API key then you can replace it above without losing your settings.",
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.Companion.padding(top = 4.dp)
                    )

                    HowToObtainAPIKeyView().Content(themeStream, modifier = Modifier.Companion.padding(top = 44.dp))
                    SettingsBottomSpace()
                }
            }
        }
    )
}

@Preview
@Composable
fun PreviewConfigureAPIKeyView() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        ConfigureAPIKeyView(
            FakeCredentialStore(),
            NavHostController(LocalContext.current),
            MutableStateFlow(AppTheme.Companion.demo()),
            modifier = Modifier.Companion
        )
    }
}