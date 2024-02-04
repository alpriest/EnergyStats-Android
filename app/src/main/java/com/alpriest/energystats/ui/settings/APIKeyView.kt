package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun ConfigureAPIKeyView(store: CredentialStore, navController: NavController) {
    var apiKey by rememberSaveable { mutableStateOf(store.getApiKey() ?: "") }

    ContentWithBottomButtonPair(navController, onSave = { store.store(apiKey) },
        content = { modifier ->
            SettingsPage(modifier) {
                SettingsColumnWithChild {
                    Text(
                        "API Key",
                        color = colors.onSecondary
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { androidx.compose.material3.Text(stringResource(R.string.api_key)) },
                        singleLine = true,
                        textStyle = TextStyle(colors.onSecondary),
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun PreviewConfigureAPIKeyView() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        ConfigureAPIKeyView(FakeCredentialStore(), NavHostController(LocalContext.current))
    }
}
