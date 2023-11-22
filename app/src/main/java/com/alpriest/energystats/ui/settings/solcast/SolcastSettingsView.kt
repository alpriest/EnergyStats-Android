package com.alpriest.energystats.ui.settings.solcast

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.ClickableUrlText
import com.alpriest.energystats.ui.flow.roundedToString
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        val sites = viewModel.sitesStream.collectAsState().value

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
                        text = stringResource(R.string.solcast_description)
                    )
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            site.name,
            style = TextStyle.Default.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Row(title = "Latitude", value = site.lat)
        Row(title = "Longitude", value = site.lng)
        Row(title = "AC Capacity (inverters)", value = site.acCapacity)
        site.dcCapacity?.let {
            Row(title = "DC Capacity (modules)", value = it)
        }
        Row(title = "Azimuth", value = site.azimuth)
        Row(title = "Tilt", value = site.tilt)
        site.installDate?.let {
            Row(title = "Install Date", value = it.monthYear())
        }
    }
}

private fun LocalDate.monthYear(): String {
    val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
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
            style = MaterialTheme.typography.bodySmall
        )
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