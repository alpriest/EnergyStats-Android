package com.alpriest.energystats.presentation

import android.content.Context
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.models.BatteryViewModel
import com.alpriest.energystats.shared.models.DataCeiling
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.PowerFlowStringsSettings
import com.alpriest.energystats.shared.models.QueryDate
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.TotalsViewModel
import com.alpriest.energystats.shared.models.network.ReportType
import com.alpriest.energystats.shared.network.FoxAPIService
import com.alpriest.energystats.shared.network.NetworkCache
import com.alpriest.energystats.shared.network.NetworkDemoSwitchingFacade
import com.alpriest.energystats.shared.network.NetworkService
import com.alpriest.energystats.shared.network.NetworkValueCleaner
import com.alpriest.energystats.shared.network.RequestData
import com.alpriest.energystats.shared.services.CurrentStatusCalculator
import com.alpriest.energystats.sync.SharedPreferencesConfigStore
import kotlinx.coroutines.CoroutineScope
import java.time.Instant

/**
 * Extracted refresh logic so it can be called from either a complication data source or the Wear VM.
 *
 * - Reads creds and throttling state from [com.alpriest.energystats.sync.SharedPreferencesConfigStore]
 * - Performs the network fetch
 * - Writes results back via `store.applyAndNotify { ... }`
 */
class WearDataRefresher(
    private val context: Context,
    private val store: SharedPreferencesConfigStore,
    private val scope: CoroutineScope
) {
    suspend fun refresh(): WearDataRefreshResult {
        val deviceSN = store.selectedDeviceSN
        if (deviceSN.isNullOrEmpty() || store.apiKey.isNullOrEmpty()) {
            store.lastUpdatedTime = Instant.now().minusSeconds(10 * 60)
            return WearDataRefreshResult.MissingCreds(context.getString(R.string.no_device_api_key_found))
        }

        val now = Instant.now()
        if (store.lastUpdatedTime.isAfter(now.minusSeconds(4 * 60))) {
            return WearDataRefreshResult.SkippedRecent
        }

        return try {
            val requestData = RequestData(
                apiKey = { store.apiKey ?: "" },
                userAgent = "Energy Stats Android WearOS"
            )
            val networking = NetworkService(
                NetworkValueCleaner(
                    NetworkDemoSwitchingFacade(
                        api = NetworkCache(api = FoxAPIService(requestData)),
                        isDemoUser = { store.apiKey == "demo" }
                    ),
                    { DataCeiling.None }
                )
            )

            val reals = networking.fetchRealData(
                deviceSN,
                listOf(
                    "SoC",
                    "SoC_1",
                    "pvPower",
                    "feedinPower",
                    "gridConsumptionPower",
                    "generationPower",
                    "meterPower2",
                    "batChargePower",
                    "batDischargePower",
                    "ResidualEnergy",
                    "batTemperature",
                    "batTemperature_1",
                    "batTemperature_2"
                )
            )

            val config = WearConfig(
                store.shouldInvertCT2,
                store.shouldCombineCT2WithPVPower,
                PowerFlowStringsSettings.Companion.defaults,
                store.shouldCombineCT2WithLoadsPower,
                store.allowNegativeLoad,
                store.showGridTotals
            )
            val device = Device(deviceSN, true, null, "", true, "", null, "")

            val currentStatusCalculator = CurrentStatusCalculator(
                reals,
                device,
                config,
                scope
            )
            val values = currentStatusCalculator.currentValuesStream.value
            val batteryViewModel = BatteryViewModel.Companion.make(device, reals)

            val totals = if (!config.showGridTotals) {
                null
            } else {
                TotalsViewModel(
                    reports = networking.fetchReport(
                        deviceSN = device.deviceSN,
                        variables = listOf(ReportVariable.FeedIn, ReportVariable.GridConsumption),
                        queryDate = QueryDate.Companion.invoke(),
                        reportType = ReportType.month
                    ),
                    generationViewModel = null
                )
            }

            store.applyAndNotify {
                lastUpdatedTime = Instant.now()
                batteryChargeLevel = batteryViewModel.chargeLevel
                solarGenerationAmount = values.solarPower
                houseLoadAmount = values.homeConsumption
                gridAmount = values.grid
                batteryChargeAmount = batteryViewModel.chargePower
                totalExport = totals?.grid
                totalImport = totals?.loads
            }

            WearDataRefreshResult.Success
        } catch (t: Exception) {
            WearDataRefreshResult.Error(t, t.message ?: "Failed to refresh")
        }
    }
}

sealed class WearDataRefreshResult {
    data object Success : WearDataRefreshResult()
    data object SkippedRecent : WearDataRefreshResult()
    data class MissingCreds(val message: String) : WearDataRefreshResult()
    data class Error(val throwable: Exception?, val message: String) : WearDataRefreshResult()
}
