package com.alpriest.energystats.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.FoxServerError
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PeakShavingSettingsViewModelFactory(
    private val configManager: ConfigManaging, private val networking: Networking
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PeakShavingSettingsViewModel(configManager, networking) as T
    }
}

class PeakShavingSettingsViewModel(
    private val configManager: ConfigManaging, private val networking: Networking
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState>(LoadState.Inactive)
    val uiState: StateFlow<LoadState> = _state

    private val _importLimit = MutableStateFlow("")
    var importLimit: MutableStateFlow<String> = _importLimit

    private val _soc = MutableStateFlow("")
    var soc: MutableStateFlow<String> = _soc

    private val _supported = MutableStateFlow(false)
    var supported: MutableStateFlow<Boolean> = _supported

    fun load(context: Context) {
        val selectedDeviceSN = configManager.selectedDeviceSN ?: return
        if (_state.value is LoadState.Error) return

        _state.value = LoadState.Active(context.getString(R.string.loading))

        viewModelScope.launch {
            try {
                val settings = networking.fetchPeakShavingSettings(selectedDeviceSN)

                _importLimit.value = settings.importLimit.value.removingEmptyDecimals()
                _soc.value = settings.soc.value
                _supported.value = true

                _state.value = LoadState.Inactive
            } catch (e: Exception) {
                val errorMessage = context.getString(R.string.failed_to_load_settings)
                _state.value = when (e) {
                    is FoxServerError -> if (e.errno == 40257) {
                        LoadState.Inactive
                    } else {
                        LoadState.Error(e, errorMessage)
                    }

                    else -> LoadState.Error(e, errorMessage)
                }
            }
        }
    }

    fun save(context: Context) {
        if (_state.value != LoadState.Inactive) {
            return
        }
        val selectedDeviceSN = configManager.selectedDeviceSN ?: return
        _state.value = LoadState.Active(context.getString(R.string.saving))

        viewModelScope.launch {
            try {
                networking.setPeakShavingSettings(selectedDeviceSN, importLimit.value.toDouble(), soc.value.toInt())

                _state.value = LoadState.Inactive
            } catch (e: Exception) {
                _state.value = LoadState.Error(e, context.getString(R.string.failed_to_save_settings))
            }
        }
    }
}

class PeakShavingSettingsView(
    private val configManager: ConfigManaging, private val network: Networking, val navController: NavController
) {

    @Composable
    fun Content(modifier: Modifier, viewModel: PeakShavingSettingsViewModel = viewModel(factory = PeakShavingSettingsViewModelFactory(configManager, network))) {
        val context = LocalContext.current
        val supported = viewModel.supported.collectAsState().value

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        when (val loadState = viewModel.uiState.collectAsState().value) {
            is LoadState.Inactive -> {
                if (supported) {
                    Loaded(viewModel, navController)
                } else {
                    SettingsPage(modifier) {
                        SettingsColumn(error = stringResource(R.string.peak_shaving_is_not_available)) { }
                    }
                }
            }

            is LoadState.Active -> {
                SettingsPage(modifier) {
                    CircularProgressIndicator()
                }
            }

            is LoadState.Error -> {
                SettingsPage(modifier) {
                    Text("Error: $loadState")
                }
            }
        }
    }

    @Composable
    fun Loaded(viewModel: PeakShavingSettingsViewModel, navController: NavController) {
        val importLimit = viewModel.importLimit.collectAsState().value
        val soc = viewModel.soc.collectAsState().value
        val explanation = stringResource(R.string.peak_shaving_explanation, importLimit, soc)
        val context = LocalContext.current

        ContentWithBottomButtonPair(navController, onSave = {
            viewModel.save(context)
        }, content = { modifier ->
            SettingsColumnWithChild(
                modifier = modifier,
                footer = explanation
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        stringResource(R.string.import_limit),
                        Modifier.weight(1.0f),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    OutlinedTextField(
                        value = importLimit,
                        onValueChange = { viewModel.importLimit.value = it.filter { it.isDigit() } },
                        modifier = Modifier.width(130.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSecondary),
                        trailingIcon = { Text("kW", color = MaterialTheme.colorScheme.onSecondary) })
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        stringResource(R.string.battery_threshold_soc),
                        Modifier.weight(1.0f),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    OutlinedTextField(
                        value = soc,
                        onValueChange = { viewModel.soc.value = it.filter { it.isDigit() } },
                        modifier = Modifier.width(130.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSecondary),
                        trailingIcon = { Text("%", color = MaterialTheme.colorScheme.onSecondary) })
                }
            }
        })
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(widthDp = 400)
@Composable
fun PeakShavingSettingsViewPreview() {
    EnergyStatsTheme {
        SettingsPage(Modifier) {
            PeakShavingSettingsView(
                FakeConfigManager(), DemoNetworking(), NavHostController(LocalContext.current)
            ).Loaded(
                viewModel = PeakShavingSettingsViewModel(FakeConfigManager(), DemoNetworking()), NavHostController(LocalContext.current)
            )
        }
    }
}

fun String.removingEmptyDecimals(): String {
    val doubleValue = this.toDoubleOrNull()
    return if (doubleValue != null) {
        if (doubleValue % 1 == 0.0) {
            doubleValue.toInt().toString()
        } else {
            doubleValue.toString()
        }
    } else {
        this
    }
}