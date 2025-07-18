package com.alpriest.energystats.ui.settings.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class BatterySOCSettings(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(viewModel: BatterySOCSettingsViewModel = viewModel(factory = BatterySOCSettingsViewModelFactory(network, configManager)), modifier: Modifier) {
        val minSOC = viewModel.minSOCStream.collectAsState().value
        val minSOConGrid = viewModel.minSOConGridStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state
        val context = LocalContext.current

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load(context)
        }
        trackScreenView("Battery Charge Levels", "BatterySOCSettings")

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(
                loadState.ex,
                loadState.reason,
                loadState.allowRetry,
                onRetry = { viewModel.load(context) },
                onLogout = { userManager.logout() }
            )
            is LoadState.Inactive ->
                ContentWithBottomButtonPair(navController, onConfirm = { viewModel.save(context) }, modifier = modifier, content = { innerModifier ->
                    SettingsPage(innerModifier) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(colorScheme.surface)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    stringResource(R.string.min_soc),
                                    Modifier.weight(1.0f),
                                    color = colorScheme.onSecondary
                                )
                                OutlinedTextField(
                                    value = minSOC,
                                    onValueChange = { viewModel.minSOCStream.value = it.filter { it.isDigit() } },
                                    modifier = Modifier.width(100.dp),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = colorScheme.onSecondary),
                                    trailingIcon = { Text("%", color = colorScheme.onSecondary) }
                                )
                            }

                            Text(
                                stringResource(R.string.minsoc_description),
                                color = colorScheme.onSecondary,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 4.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .background(colorScheme.surface)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    stringResource(R.string.min_soc_on_grid),
                                    Modifier.weight(1.0f),
                                    color = colorScheme.onSecondary
                                )
                                OutlinedTextField(
                                    value = minSOConGrid,
                                    onValueChange = { viewModel.minSOConGridStream.value = it.filter { it.isDigit() } },
                                    modifier = Modifier.width(100.dp),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = colorScheme.onSecondary),
                                    trailingIcon = { Text("%", color = colorScheme.onSecondary) }
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 4.dp)
                            ) {
                                Text(
                                    stringResource(R.string.minsocgrid_description),
                                    color = colorScheme.onSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    stringResource(R.string.minsoc_detail),
                                    color = colorScheme.onSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    stringResource(R.string.minsoc_notsure_footnote),
                                    color = colorScheme.onSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                })
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
            userManager = FakeUserManager()
        ).Content(modifier = Modifier)
    }
}
