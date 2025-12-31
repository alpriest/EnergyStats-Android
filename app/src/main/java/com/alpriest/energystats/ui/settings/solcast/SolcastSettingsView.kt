package com.alpriest.energystats.ui.settings.solcast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.alpriest.energystats.BuildConfig
import com.alpriest.energystats.R
import com.alpriest.energystats.helpers.monthYearString
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.shared.models.SolcastSite
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.helpers.ClickableUrlText
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsBottomSpace
import com.alpriest.energystats.ui.settings.SettingsCheckbox
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPaddingValues
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.summary.DemoSolarForecasting
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.OutlinedESButton

class SolcastSettingsView(
    private val navController: NavController,
    private val configManager: ConfigManaging,
    private val userManager: UserManaging,
    private val solarForecastingProvider: () -> SolcastCaching
) {
    @Composable
    fun Content(
        viewModel: SolcastSettingsViewModel = viewModel(
            factory = SolcastSettingsViewModelFactory(
                configManager = configManager,
                solarForecastingProvider = solarForecastingProvider
            )
        ),
        modifier: Modifier
    ) {
        val viewData = viewModel.viewDataStream.collectAsStateWithLifecycle().value
        trackScreenView("Solar Prediction", "SolcastSettingsView")
        val fetchSolcastOnAppLaunch = rememberSaveable { mutableStateOf(configManager.fetchSolcastOnAppLaunch) }

        MonitorAlertDialog(viewModel, userManager)

        ContentWithBottomButtonPair(
            navController,
            onConfirm = { viewModel.save() },
            dirtyStateFlow = viewModel.dirtyState,
            content = { innerModifier ->
                SettingsPage(innerModifier) {
                    SettingsColumn(padding = SettingsPaddingValues.withVertical()) {
                        ClickableUrlText(
                            text = stringResource(R.string.solcast_how_to_find_keys),
                            modifier = Modifier.padding(bottom = 8.dp),
                            textStyle = TextStyle(colorScheme.onSecondary),
                            themeStream = configManager.themeStream
                        )

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = viewData.apiKey,
                            onValueChange = { viewModel.didChange(it) },
                            label = { Text(stringResource(R.string.api_key)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            textStyle = TextStyle(colorScheme.onSecondary),
                        )
                    }

                    SettingsColumn(
                        padding = SettingsPaddingValues.withVertical(),
                        footer = stringResource(R.string.solcast_fetch_on_launch)
                    ) {
                        SettingsCheckbox(
                            title = stringResource(R.string.fetch_solar_forecast_on_app_launch),
                            state = fetchSolcastOnAppLaunch,
                            onUpdate = { configManager.fetchSolcastOnAppLaunch = it }
                        )
                    }

                    viewData.sites.forEach {
                        SolcastSiteView(it)
                    }

                    OutlinedESButton(
                        onClick = { viewModel.removeKey() }
                    ) {
                        Text(stringResource(R.string.remove_key))
                    }

                    SettingsBottomSpace()
                }
            },
            modifier = modifier
        )
    }
}

@Composable
fun SolcastSiteView(site: SolcastSite) {
    SettingsColumn(
        modifier = Modifier.fillMaxWidth(),
        padding = SettingsPaddingValues.withVertical()
    ) {
        Text(
            site.name,
            style = TextStyle.Default.copy(fontWeight = FontWeight.Bold, color = colorScheme.onSecondary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Map(site)

            Column {
                Row(title = stringResource(R.string.latitude), value = site.lat)
                Row(title = stringResource(R.string.longitude), value = site.lng)
                Row(title = stringResource(R.string.ac_capacity_inverters), value = "${site.acCapacity} kW")
                site.dcCapacity?.let {
                    Row(title = stringResource(R.string.dc_capacity_modules), value = "$it kW")
                }
                Row(title = stringResource(R.string.azimuth), value = site.azimuth)
                Row(title = stringResource(R.string.tilt), value = site.tilt)
                site.installDate?.let {
                    Row(title = stringResource(R.string.install_date), value = it.monthYearString())
                }
            }
        }
    }
}

@Composable
private fun Map(site: SolcastSite) {
    val apiKey = BuildConfig.GOOGLE_MAPS_APIKEY
    val location = "${site.lat},${site.lng}"
    val zoom = 18
    val size = "120x120"

    val mapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=$location&zoom=$zoom&size=$size&key=$apiKey"

    AsyncImage(
        model = mapUrl,
        contentDescription = "Map",
        modifier = Modifier.size(width = 120.dp, height = 120.dp)
    )
}

@Composable
private fun Row(title: String, value: Int) {
    Row(title, value = "$value")
}

@Composable
private fun Row(title: String, value: Double) {
    Row(title, value = "$value")
}

@Composable
private fun Row(title: String, value: String) {
    Row {
        Text(
            title,
            modifier = Modifier.weight(1.0f),
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSecondary
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSecondary
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun SolcastSettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SolcastSettingsView(
            navController = NavHostController(LocalContext.current),
            FakeConfigManager(),
            FakeUserManager(),
            { DemoSolarForecasting() }
        ).Content(modifier = Modifier)
    }
}