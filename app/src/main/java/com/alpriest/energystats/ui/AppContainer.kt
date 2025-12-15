package com.alpriest.energystats.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.alpriest.energystats.services.FoxAPIService
import com.alpriest.energystats.services.InMemoryLoggingNetworkStore
import com.alpriest.energystats.services.NetworkCache
import com.alpriest.energystats.services.NetworkFacade
import com.alpriest.energystats.services.NetworkService
import com.alpriest.energystats.services.NetworkValueCleaner
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.stores.SharedPreferencesConfigStore
import com.alpriest.energystats.stores.SharedPreferencesCredentialStore
import com.alpriest.energystats.stores.WidgetDataSharer
import com.alpriest.energystats.stores.WidgetDataSharing
import com.alpriest.energystats.ui.flow.BannerAlertManager
import com.alpriest.energystats.ui.flow.BannerAlertManaging
import com.alpriest.energystats.ui.login.ConfigManager
import com.alpriest.energystats.ui.login.UserManager
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.settings.inverter.CT2DisplayMode
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStore
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStoring
import com.alpriest.energystats.ui.settings.solcast.Solcast
import com.alpriest.energystats.ui.settings.solcast.SolcastCache
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.summary.DemoSolarForecasting
import com.alpriest.energystats.ui.theme.AppTheme
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import kotlinx.coroutines.flow.MutableStateFlow

class AppContainer(private val context: Context) {
    val templateStore: TemplateStoring by lazy {
        TemplateStore(configManager)
    }
    val solarForecastingProvider: () -> SolcastCaching = {
        if (config.isDemoUser) {
            DemoSolarForecasting()
        } else {
            SolcastCache(Solcast(configManager), context)
        }
    }
    val networkStore: InMemoryLoggingNetworkStore = InMemoryLoggingNetworkStore.shared
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(
            "com.alpriest.energystats",
            Context.MODE_PRIVATE
        )
    internal val credentialStore: CredentialStore = SharedPreferencesCredentialStore(sharedPreferences)
    val config = SharedPreferencesConfigStore(sharedPreferences)
    var filePathChooser: ActivityResultLauncher<String>? = null
    var filePathChooserCallback: ((Uri) -> Unit)? = null
    val themeStream: MutableStateFlow<AppTheme> = MutableStateFlow(
        AppTheme(
            useLargeDisplay = config.useLargeDisplay,
            useColouredLines = config.useColouredFlowLines,
            showBatteryTemperature = config.showBatteryTemperature,
            showBatteryEstimate = config.showBatteryEstimate,
            decimalPlaces = config.decimalPlaces,
            showSunnyBackground = config.showSunnyBackground,
            showUsableBatteryOnly = config.showUsableBatteryOnly,
            selfSufficiencyEstimateMode = SelfSufficiencyEstimateMode.fromInt(config.selfSufficiencyEstimateMode),
            showFinancialSummary = config.showFinancialSummary,
            displayUnit = DisplayUnit.fromInt(config.displayUnit),
            showInverterTemperatures = config.showInverterTemperatures,
            showInverterIcon = config.showInverterIcon,
            showHomeTotal = config.showHomeTotal,
            shouldInvertCT2 = config.shouldInvertCT2,
            showGridTotals = config.showGridTotals,
            showInverterTypeNameOnPowerflow = config.showInverterTypeNameOnPowerflow,
            showInverterStationNameOnPowerflow = config.showInverterStationNameOnPowerflow,
            showLastUpdateTimestamp = config.showLastUpdateTimestamp,
            solarRangeDefinitions = config.solarRangeDefinitions,
            shouldCombineCT2WithPVPower = config.shouldCombineCT2WithPVPower,
            showGraphValueDescriptions = config.showGraphValueDescriptions,
            parameterGroups = config.parameterGroups,
            colorTheme = ColorThemeMode.fromInt(config.colorTheme),
            solcastSettings = config.solcastSettings,
            dataCeiling = DataCeiling.fromInt(config.dataCeiling),
            totalYieldModel = TotalYieldModel.fromInt(config.totalYieldModel),
            showFinancialSummaryOnFlowPage = config.showFinancialSummaryOnFlowPage,
            separateParameterGraphsByUnit = config.separateParameterGraphsByUnit,
            currencySymbol = config.currencySymbol,
            showBatterySOCAsPercentage = config.showBatterySOCAsPercentage,
            shouldCombineCT2WithLoadsPower = config.shouldCombineCT2WithLoadsPower,
            powerFlowStrings = config.powerFlowStrings,
            truncatedYAxisOnParameterGraphs = config.truncatedYAxisOnParameterGraphs,
            showInverterScheduleQuickLink = config.showInverterScheduleQuickLink,
            ct2DisplayMode = CT2DisplayMode.fromInt(config.ct2DisplayMode),
            showStringTotalsAsPercentage = config.showStringTotalsAsPercentage,
            detectedActiveTemplate = null,
            showInverterConsumption = config.showInverterConsumption,
            showBatterySOCOnDailyStats = config.showBatterySOCOnDailyStats,
            allowNegativeHouseLoad = config.allowNegativeHouseLoad
        )
    )

    val networking: Networking by lazy {
        val chucker = ChuckerInterceptor.Builder(context)
            .collector(
                ChuckerCollector(
                    context,
                    showNotification = false,
                    retentionPeriod = RetentionManager.Period.ONE_HOUR
                )
            )
            .build()

        NetworkService(
            NetworkValueCleaner(
                NetworkFacade(
                    api = NetworkCache(api = FoxAPIService(credentialStore, networkStore, chucker)),
                    isDemoUser = { config.isDemoUser }
                ),
                themeStream
            )
        )
    }

    val configManager: ConfigManaging by lazy {
        ConfigManager(
            config = config,
            networking = networking,
            appVersion = getAppVersionName(context),
            themeStream
        )
    }

    val widgetDataSharer: WidgetDataSharing by lazy {
        WidgetDataSharer(config)
    }

    val userManager: UserManaging by lazy {
        UserManager(configManager, credentialStore)
    }

    val bannerAlertManager: BannerAlertManaging by lazy {
        BannerAlertManager()
    }

    fun openAppInPlayStore() {
        val uri = ("market://details?id=" + context.packageName).toUri()
        val goToMarketIntent = Intent(Intent.ACTION_VIEW, uri)

        val flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or FLAG_ACTIVITY_NEW_TASK
        goToMarketIntent.addFlags(flags)

        try {
            context.startActivity(goToMarketIntent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                ("http://play.google.com/store/apps/details?id=" + context.packageName).toUri()
            )

            context.startActivity(intent)
        }
    }

    fun buyMeACoffee() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        val data = "https://buymeacoffee.com/alpriest".toUri()
        intent.data = data
        context.startActivity(intent)
    }

    fun writeToTempFile(baseFilename: String, text: String): Uri? {
        val file = kotlin.io.path.createTempFile(baseFilename + "_", ".csv").toFile()
        file.writeText(text)
        return FileProvider.getUriForFile(context, "com.alpriest.energystats.ui.statsgraph.ExportFileProvider", file)
    }

    fun showFileChooser(filename: String, action: (Uri) -> Unit) {
        filePathChooserCallback = action
        filePathChooser?.launch(filename)
    }
}

fun getAppVersionName(context: Context): String {
    var appVersionName = ""
    try {
        appVersionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return appVersionName
}
