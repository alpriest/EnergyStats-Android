package com.alpriest.energystats.ui.settings.devicesettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.AlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPadding
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class DeviceSettingItemView(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val item: DeviceSettingsItem,
    private val navController: NavHostController
) {
    @Composable
    fun Content(modifier: Modifier, viewModel: DeviceSettingsItemViewModel = viewModel(factory = DeviceSettingsItemViewModelFactory(configManager, network, item))) {
        val message = viewModel.alertDialogMessage.collectAsState().value

        LaunchedEffect(null) {
            viewModel.load()
        }

        message?.let {
            AlertDialog(message = it.message ?: "Unknown error", onDismiss = {
                viewModel.resetDialogMessage()
            })
        }

        when (val loadState = viewModel.uiState.collectAsState().value) {
            is LoadState.Inactive -> {
                LoadedView(viewModel, modifier, navController)
            }

            is LoadState.Active -> {
                LoadingView(loadState.value)
            }

            is LoadState.Error -> {
                Text("Error: $loadState")
            }
        }
    }

    @Composable
    private fun LoadedView(viewModel: DeviceSettingsItemViewModel, modifier: Modifier, navController: NavController) {
        val context = LocalContext.current
        val value = viewModel.valueStream.collectAsState().value
        val unit = viewModel.unitStream.collectAsState().value
        val annotatedString = buildAnnotatedString {
            append(viewModel.item.description(context))
            append("\n\n")
            append(viewModel.item.behaviour(context))
            append("\n\n")

            withStyle(style = SpanStyle(color = colorScheme.error)) {
                append(stringResource(R.string.device_settings_warning))
            }
        }

        ContentWithBottomButtonPair(
            navController,
            modifier = modifier,
            onConfirm = {
                viewModel.save()
            },
            dirtyStateFlow = viewModel.dirtyState,
            content = { modifier ->
                SettingsPage(modifier) {
                    SettingsColumn {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                viewModel.item.title(context),
                                Modifier.weight(1.0f),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            OutlinedTextField(
                                value = value,
                                onValueChange = { viewModel.valueStream.value = it.filter { it.isDigit() } },
                                modifier = Modifier.width(130.dp),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = colorScheme.onSecondary),
                                trailingIcon = { Text(unit, color = colorScheme.onSecondary) }
                            )
                        }
                    }

                    Text(
                        annotatedString,
                        color = colorScheme.onSecondary,
                        modifier = Modifier.padding(SettingsPadding.PANEL_INNER_HORIZONTAL)
                    )
                }
            })
    }
}

@Composable
@Preview(showBackground = true)
fun DeviceSettingItemViewPreview() {
    EnergyStatsTheme {
        Surface {
            DeviceSettingItemView(
                network = DemoNetworking(),
                configManager = FakeConfigManager(),
                item = DeviceSettingsItem.MaxSoc,
                navController = NavHostController(LocalContext.current)
            ).Content(Modifier)
        }
    }
}
