package com.alpriest.energystats.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.helpers.w
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.settings.inverter.SettingsRow
import kotlinx.coroutines.flow.MutableStateFlow

class PowerStationSettingsView(private val configManager: ConfigManaging) {
    @Composable
    fun Content(viewModel: PowerStationSettingsViewModel = viewModel(factory = PowerStationSettingsViewModelFactory(configManager))) {
        val context = LocalContext.current
        trackScreenView("Power Station", "PowerStationSettingsView")

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        configManager.powerStationDetail?.let {
            SettingsPage {
                SettingsColumn {
                    SettingsRow(stringResource(R.string.name), it.stationName)
                    SettingsRow(stringResource(R.string.capacity), it.capacity.w())
                    SettingsRow(stringResource(R.string.timezone), it.timezone)
                }
            }
        }
    }
}

class PowerStationSettingsViewModel(
    val config: ConfigManaging
) : ViewModel() {
    var uiState = MutableStateFlow<LoadState>(LoadState.Inactive)

    suspend fun load(context: Context) {
        uiState.value = LoadState.Active.Loading

        runCatching {
            try {
                config.fetchPowerStationDetail()
            } catch (ex: Exception) {
                uiState.value = LoadState.Error(ex, ex.localizedMessage ?: context.getString(R.string.failed_to_load_settings))
            }
        }
    }
}

class PowerStationSettingsViewModelFactory(
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PowerStationSettingsViewModel(configManager) as T
    }
}
