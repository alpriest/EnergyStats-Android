package com.alpriest.energystats

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.helpers.WearableApiAvailability
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.BatteryViewModel
import com.alpriest.energystats.shared.models.Schedule
import com.alpriest.energystats.shared.models.SchedulePhase
import com.alpriest.energystats.shared.models.SharedDataKeys
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.shared.services.CurrentValues
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.settings.inverter.schedule.asSchedule
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val CREDS_PATH = "/auth/creds"

class PreHomeViewModel(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val solarForecastProvider: () -> SolcastCaching,
    private val credentialStore: CredentialStore
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    internal fun loadData(context: Context) {
        viewModelScope.launch {
            try {
                network.fetchErrorMessages()
            } catch (_: Exception) {
            }
        }

        viewModelScope.launch {
            refreshSolcast()
        }

        viewModelScope.launch {
            fetchCurrentInverterSchedule()
        }

        viewModelScope.launch {
            credentialStore.getApiKey()?.let {
                WatchSyncManager().sendWatchConfigData(context, it, configManager)
            }
        }

        viewModelScope.launch {
            configManager.currentDevice.collect {
                credentialStore.getApiKey()?.let {
                    WatchSyncManager().sendWatchConfigData(context, it, configManager)
                }
            }
        }
    }

    private suspend fun refreshSolcast() {
        if (!configManager.fetchSolcastOnAppLaunch) return
        val apiKey = configManager.solcastSettings.apiKey ?: return

        val service = solarForecastProvider()

        try {
            configManager.solcastSettings.sites.forEach { site ->
                service.fetchForecast(site, apiKey, ignoreCache = false)
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    private suspend fun fetchCurrentInverterSchedule() {
        if (!configManager.showInverterScheduleQuickLink) return
        val deviceSN = configManager.selectedDeviceSN ?: return
        try {
            val scheduleResponse = network.fetchCurrentSchedule(deviceSN)
            val schedule = Schedule.create(scheduleResponse)

            configManager.scheduleTemplates.forEach { template ->
                val templatePhases = template.asSchedule().phases
                    .sortedWith { first, second -> first.start.compareTo(second.start) }
                val match = templatePhases.zip(schedule.phases).all { (templatePhase, schedulePhase) ->
                    templatePhase.isEqualConfiguration(schedulePhase)
                }
                if (match) {
                    configManager.detectedActiveTemplate = template.name
                }
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    private fun SchedulePhase.isEqualConfiguration(other: SchedulePhase): Boolean {
        return start == other.start &&
                end == other.end &&
                mode == other.mode &&
                minSocOnGrid == other.minSocOnGrid &&
                forceDischargePower == other.forceDischargePower &&
                forceDischargeSOC == other.forceDischargeSOC &&
                maxSOC == other.maxSOC
    }
}

@Composable
fun PreHomeView(appContainer: AppContainer, viewModel: PreHomeViewModel) {
    MonitorAlertDialog(viewModel, appContainer.userManager)
    val context = LocalContext.current

    LaunchedEffect(null) {
        viewModel.loadData(context)
    }

    MainAppView(appContainer)
}

class WatchSyncManager() {
    private companion object {
        private const val TAG = "WatchSyncManager"
    }

    suspend fun sendWatchStatsData(
        context: Context,
        currentValuesStream: StateFlow<CurrentValues>,
        battery: BatteryViewModel,
        configManaging: ConfigManaging,
        apiKey: String?
    ) {
        val dataClient = Wearable.getDataClient(context)

        if (!WearableApiAvailability.isAvailable(dataClient)) {
            Log.w(TAG, "Wearable DataClient not available on this device")
            return
        }

        val currentValues = currentValuesStream.value

        val putDataMapRequest = PutDataMapRequest.create(CREDS_PATH).apply {
            dataMap.putDouble(SharedDataKeys.SOLAR_GENERATION_AMOUNT, currentValues.solarPower)
            dataMap.putDouble(SharedDataKeys.HOUSE_LOAD_AMOUNT, currentValues.homeConsumption)
            dataMap.putDouble(SharedDataKeys.GRID_AMOUNT, currentValues.grid)
            dataMap.putDouble(SharedDataKeys.BATTERY_CHARGE_LEVEL, battery.chargeLevel)
            dataMap.putDouble(SharedDataKeys.BATTERY_CHARGE_AMOUNT, battery.chargePower)
        }

        addConfig(putDataMapRequest, apiKey, configManaging)

        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()

        try {
            val result = dataClient.putDataItem(putDataRequest).await()
            Log.d(TAG, "Sent DataItem to Wear: uri=${result.uri} path=${result.uri.path}")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to send DataItem to Wear")
        }
    }

    suspend fun sendWatchConfigData(context: Context, apiKey: String, config: ConfigManaging) {
        val dataClient = Wearable.getDataClient(context)

        if (!WearableApiAvailability.isAvailable(dataClient)) {
            return
        }

        val putDataMapRequest = PutDataMapRequest.create(CREDS_PATH)
        addConfig(putDataMapRequest, apiKey, config)
        val putReq = putDataMapRequest.asPutDataRequest().setUrgent()

        try {
            val result = dataClient.putDataItem(putReq).await()
            Log.d(TAG, "Sent DataItem to Wear: uri=${result.uri} path=${result.uri.path}")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to send DataItem to Wear")
        }
    }

    fun addConfig(putDataMapRequest: PutDataMapRequest, token: String?, configManaging: ConfigManaging) {
        val solarRangeDataMap = DataMap().apply {
            putDouble(SharedDataKeys.THRESHOLD_1, configManaging.solarRangeDefinitions.threshold1)
            putDouble(SharedDataKeys.THRESHOLD_2, configManaging.solarRangeDefinitions.threshold2)
            putDouble(SharedDataKeys.THRESHOLD_3, configManaging.solarRangeDefinitions.threshold3)
        }

        putDataMapRequest.apply {
            token?.let {
                dataMap.putString(SharedDataKeys.TOKEN, it)
            }
            configManaging.selectedDeviceSN?.let {
                dataMap.putString(SharedDataKeys.DEVICE_SN, it)
            }
            dataMap.putBoolean(SharedDataKeys.SHOW_GRID_TOTALS, configManaging.showGridTotals)
            dataMap.putString(SharedDataKeys.BATTERY_CAPACITY, configManaging.batteryCapacity)
            dataMap.putBoolean(SharedDataKeys.SHOULD_INVERT_CT2, configManaging.shouldInvertCT2)
            dataMap.putDouble(SharedDataKeys.MIN_SOC, configManaging.minSOC)
            dataMap.putBoolean(SharedDataKeys.SHOULD_COMBINE_CT2_WITH_PV, configManaging.shouldCombineCT2WithPVPower)
            dataMap.putBoolean(SharedDataKeys.SHOW_USABLE_BATTERY_ONLY, configManaging.showUsableBatteryOnly)
            dataMap.putDataMap(SharedDataKeys.SOLAR_RANGE_DEFINITIONS, solarRangeDataMap)

            // Force a change each time so the watch definitely sees an update.
            dataMap.putLong(SharedDataKeys.UPDATED_AT, System.currentTimeMillis())
        }
    }
}