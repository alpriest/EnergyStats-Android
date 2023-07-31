package com.alpriest.energystats.ui.settings.battery

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.settings.SettingsButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.launch

class BatterySOCSettings(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController,
    private val context: Context
) {
    @Composable
    fun Content(viewModel: BatterySOCSettingsViewModel = viewModel(factory = BatterySOCSettingsViewModelFactory(network, configManager, navController, context))) {
        val minSOC = viewModel.minSOCStream.collectAsState().value
        val minSOConGrid = viewModel.minSOConGridStream.collectAsState().value
        val isActive = viewModel.activityStream.collectAsState().value
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(null) {
            viewModel.load()
        }

        isActive?.let {
            LoadingView(it)
        } ?: run {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colors.surface)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            stringResource(R.string.min_soc),
                            Modifier.weight(1.0f),
                            style = MaterialTheme.typography.h4
                        )
                        OutlinedTextField(
                            value = minSOC,
                            onValueChange = { viewModel.minSOCStream.value = it.filter { it.isDigit() } },
                            modifier = Modifier.width(100.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                            trailingIcon = { Text("%") }
                        )
                    }

                    Text(
                        stringResource(R.string.minsoc_description),
                        color = MaterialTheme.colors.onSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .background(MaterialTheme.colors.surface)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            stringResource(R.string.min_soc_on_grid),
                            Modifier.weight(1.0f),
                            style = MaterialTheme.typography.h4
                        )
                        OutlinedTextField(
                            value = minSOConGrid,
                            onValueChange = { viewModel.minSOConGridStream.value = it.filter { it.isDigit() }},
                            modifier = Modifier.width(100.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                            trailingIcon = { Text("%") }
                        )
                    }

                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text(
                            stringResource(R.string.minsocgrid_description),
                            color = MaterialTheme.colors.onSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            stringResource(R.string.minsoc_detail),
                            color = MaterialTheme.colors.onSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            stringResource(R.string.minsoc_notsure_footnote),
                            color = MaterialTheme.colors.onSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                SettingsButton(stringResource(R.string.save)) {
                    coroutineScope.launch {
                        viewModel.save()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BatterySOCSettingsViewPreview() {
    EnergyStatsTheme {
        BatterySOCSettings(
            network = DemoNetworking(),
            configManager = FakeConfigManager(),
            navController = NavHostController(LocalContext.current),
            context = LocalContext.current
        ).Content()
    }
}
