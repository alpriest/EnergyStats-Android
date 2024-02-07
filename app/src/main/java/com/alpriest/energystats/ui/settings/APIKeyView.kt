package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.login.HowToObtainAPIKeyView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ConfigureAPIKeyView(store: CredentialStore, navController: NavController, themeStream: MutableStateFlow<AppTheme>) {
    var apiKey by rememberSaveable { mutableStateOf(store.getApiKey() ?: "") }

    ContentWithBottomButtonPair(navController, onSave = { store.store(apiKey) },
        content = { modifier ->
            SettingsPage(modifier) {
                SettingsColumnWithChild {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { androidx.compose.material3.Text(stringResource(R.string.api_key)) },
                        singleLine = true,
                        textStyle = TextStyle(colors.onSecondary),
                    )

                    HowToObtainAPIKeyView(themeStream, modifier = Modifier.padding(top = 44.dp))
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
            MutableStateFlow(AppTheme.preview())
        )
    }
}
