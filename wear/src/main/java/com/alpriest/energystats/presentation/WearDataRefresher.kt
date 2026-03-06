package com.alpriest.energystats.presentation

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.alpriest.energystats.R
import com.alpriest.energystats.complication.MainComplicationService
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
import com.alpriest.energystats.sync.make
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Extracted refresh logic so it can be called from either a complication data source or the Wear VM.
 *
 * - Reads creds and throttling state from [com.alpriest.energystats.sync.SharedPreferencesConfigStore]
 * - Performs the network fetch
 * - Writes results back via `store.applyAndNotify { ... }`
 */
class WearDataRefresher(
    private val context: Context,
    private val store: SharedPreferencesConfigStore
) {
    private val refreshMutex = Mutex()

    suspend fun refresh(force: Boolean): WearDataRefreshResult {
        refreshMutex.withLock {
            val deviceSN = store.selectedDeviceSN
            if (deviceSN.isNullOrEmpty() || store.apiKey.isNullOrEmpty()) {
                store.lastUpdatedTime = Instant.now().minusSeconds(10 * 60)
                return WearDataRefreshResult.MissingCreds(context.getString(R.string.no_device_api_key_found))
            }

            val now = Instant.now()
            if (force == false && store.lastUpdatedTime.isAfter(now.minusSeconds(4 * 60))) {
                return WearDataRefreshResult.SkippedRecent
            }

            return try {
                withContext(Dispatchers.IO) {
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
                        coroutineScope = null
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
                }

                WearDataRefreshResult.Success
            } catch (t: CancellationException) {
                // This typically happens when onComplicationRequest wraps refresh() with withTimeoutOrNull.
                Log.w("WearDataRefresher", "Refresh cancelled (likely timeout)")
                throw t
            } catch (t: Exception) {
                Log.e("WearDataRefresher", "Refresh failed: ${t::class.java.simpleName}: ${t.message}", t)

                if (t.isOfflineLike()) {
                    enqueueRetry(context)  // see below
                    // IMPORTANT: don't bump lastUpdatedTime here
                    return WearDataRefreshResult.Error(null, "Offline - will retry")
                }

                return WearDataRefreshResult.Error(t, t.message ?: "Failed to refresh")
            }
        }
    }

    private fun enqueueRetry(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<WearRefreshWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .addTag("wear_refresh_retry")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "wear_refresh_retry",
            ExistingWorkPolicy.KEEP,  // don't spam
            request
        )
    }
}

class WearRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val store = SharedPreferencesConfigStore.make(applicationContext) // however you create it
        val refresher = WearDataRefresher(applicationContext, store)

        val result = refresher.refresh(force = true)

        return when (result) {
            is WearDataRefreshResult.Success,
            is WearDataRefreshResult.SkippedRecent -> {
                // Push a complication redraw now we have fresh-ish data
                MainComplicationService.requestRefresh(applicationContext)
                Result.success()
            }

            is WearDataRefreshResult.MissingCreds -> Result.failure()
            is WearDataRefreshResult.Error -> Result.retry()
        }
    }
}

sealed class WearDataRefreshResult {
    data object Success : WearDataRefreshResult()
    data object SkippedRecent : WearDataRefreshResult()
    data class MissingCreds(val message: String) : WearDataRefreshResult()
    data class Error(val throwable: Exception?, val message: String) : WearDataRefreshResult()
}

private fun Throwable.isOfflineLike(): Boolean =
    this is UnknownHostException ||
            this is ConnectException ||
            this is SocketTimeoutException ||
            (this is IOException && message?.contains("No address associated with hostname") == true)
