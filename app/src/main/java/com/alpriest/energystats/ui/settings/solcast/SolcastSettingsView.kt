package com.alpriest.energystats.ui.settings.solcast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.alpriest.energystats.BuildConfig
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.ClickableUrlText
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.summary.DemoSolarForecasting
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SolcastSettingsView(
    private val navController: NavController,
    private val configManager: ConfigManaging,
    private val solarForecastingProvider: () -> SolarForecasting
) {
    @Composable
    fun Content(
        viewModel: SolcastSettingsViewModel = viewModel(
            factory = SolcastSettingsViewModelFactory(
                configManager = configManager,
                solarForecastingProvider = solarForecastingProvider
            )
        )
    ) {
        val apiKey = viewModel.apiKeyStream.collectAsState().value
        val sites = viewModel.sitesStream.collectAsState().value

        MonitorAlertDialog(viewModel)

        ContentWithBottomButtons(navController, onSave = { viewModel.save() }, content = { modifier ->
            SettingsPage(modifier) {
                Column {
                    SettingsColumnWithChild {
                        ClickableUrlText(
                            text = stringResource(R.string.solcast_how_to_find_keys),
                            modifier = Modifier.padding(bottom = 8.dp),
                            textStyle = TextStyle(colors.onSecondary),
                            themeStream = configManager.themeStream
                        )

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = apiKey,
                            onValueChange = { viewModel.apiKeyStream.value = it },
                            label = { Text(stringResource(R.string.api_key)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            textStyle = TextStyle(colors.onSecondary),
                        )
                    }
                }

                sites.forEach {
                    SolcastSiteView(it)
                }
            }
        })
    }
}

@Composable
fun SolcastSiteView(site: SolcastSite) {
    SettingsColumnWithChild(modifier = Modifier.fillMaxWidth()) {
        Text(
            site.name,
            style = TextStyle.Default.copy(fontWeight = FontWeight.Bold, color = colors.onSecondary),
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
                    Row(title = stringResource(R.string.install_date), value = it.monthYear())
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

private fun LocalDate.monthYear(): String {
    val formatter = DateTimeFormatter.ofPattern("MMM, yyyy")
    return this.format(formatter)
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
            color = colors.onSecondary
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSecondary
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun SolcastSettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        SolcastSettingsView(
            navController = NavHostController(LocalContext.current),
            FakeConfigManager(),
            { DemoSolarForecasting() }
        ).Content()
    }
}