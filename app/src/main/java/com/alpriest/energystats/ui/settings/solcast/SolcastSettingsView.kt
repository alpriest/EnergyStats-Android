package com.alpriest.energystats.ui.settings.solcast

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.ClickableUrlText
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class SolcastSettingsView(
    private val navController: NavController,
    private val configManager: ConfigManaging,
    private val context: Context
) {
    @Composable
    fun Content(
        viewModel: SolcastSettingsViewModel = viewModel(
            factory = SolcastSettingsViewModelFactory(
                configManager = configManager,
                context = context,
                makeService = { Solcast() }
            )
        )
    ) {
        val apiKey = viewModel.apiKeyStream.collectAsState().value
        val text = stringResource(R.string.solcast_description)

        ContentWithBottomButtons(navController, onSave = { viewModel.save() }, content = { modifier ->
            SettingsPage(modifier) {
                SettingsColumnWithChild {
                    ClickableUrlText(
                        text = stringResource(R.string.solcast_how_to_find_keys),
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = apiKey,
                        onValueChange = { viewModel.apiKeyStream.value = it },
                        label = { Text(stringResource(R.string.api_key)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    ClickableUrlText(
                        text = text
                    )
                }
            }
        })
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun SolcastSettingsViewPreview() {
    val context = LocalContext.current

    EnergyStatsTheme {
        SolcastSettingsView(
            navController = NavHostController(LocalContext.current),
            FakeConfigManager(),
            context = context
        ).Content()
    }
}