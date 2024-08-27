package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.ClickableUrlText
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ConfigureAPIKeyView(store: CredentialStore, navController: NavController, themeStream: MutableStateFlow<AppTheme>) {
    var apiKey by rememberSaveable { mutableStateOf(store.getApiKey() ?: "") }

    ContentWithBottomButtonPair(navController, onSave = {
        store.store(apiKey)
        navController.popBackStack()
    },
        content = { modifier ->
            SettingsPage(modifier) {
                SettingsColumnWithChild {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { androidx.compose.material3.Text(stringResource(R.string.api_key)) },
                        singleLine = true,
                        textStyle = TextStyle(colorScheme.onSecondary),
                    )

                    Text(
                        "If you need to change your API key then you can replace it above without losing your settings.",
                        color = colorScheme.onSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    HowToObtainAPIKeyView(themeStream, modifier = Modifier.padding(top = 44.dp))
                }
            }
        }
    )
}

@Composable
fun HowToObtainAPIKeyView(themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            stringResource(R.string.how_to_get_api_key),
            modifier = Modifier.padding(bottom = 8.dp),
            color = colorScheme.onSecondary
        )
        ClickableUrlText(
            text = stringResource(R.string.api_key_step_1),
            textStyle = TextStyle(colorScheme.onSecondary),
            themeStream = themeStream
        )
        Text(stringResource(R.string.api_key_step_2), color = colorScheme.onSecondary)
        Text(stringResource(R.string.api_key_step_3), color = colorScheme.onSecondary)
        Text(stringResource(R.string.api_key_step_4), color = colorScheme.onSecondary)
        Text(stringResource(R.string.api_key_step_5), color = colorScheme.onSecondary)
        Text(stringResource(R.string.api_key_step_6), color = colorScheme.onSecondary)
        Text(stringResource(R.string.api_key_step_7), color = colorScheme.onSecondary)

        val text = stringResource(R.string.api_key_step_8)
        val key = stringResource(R.string.example_api_key)

        Text(
            buildAnnotatedString {
                append(text)

                withStyle(style = SpanStyle(color = Color.Red)) {
                    append(key)
                }
            },
            color = colorScheme.onSecondary
        )

        Text(
            stringResource(R.string.automatic_time_advice),
            modifier = Modifier.padding(vertical = 12.dp),
            color = colorScheme.onSecondary
        )

        ClickableUrlText(
            text = stringResource(R.string.api_change_reason),
            modifier = Modifier.padding(vertical = 12.dp),
            textStyle = TextStyle(colorScheme.onSecondary),
            themeStream = themeStream
        )
    }
}

@Preview
@Composable
fun PreviewConfigureAPIKeyView() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        ConfigureAPIKeyView(
            FakeCredentialStore(),
            NavHostController(LocalContext.current),
            MutableStateFlow(AppTheme.demo())
        )
    }
}
