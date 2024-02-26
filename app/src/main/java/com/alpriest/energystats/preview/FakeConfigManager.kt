package com.alpriest.energystats.preview

import com.alpriest.energystats.models.Battery
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.settings.solcast.SolcastSettings
import com.alpriest.energystats.ui.settings.solcast.SolcastSite
import com.alpriest.energystats.ui.settings.solcast.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions
import com.alpriest.energystats.ui.theme.preview
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConfigManager : ConfigManaging {
    override var colorThemeMode: ColorThemeMode = ColorThemeMode.Auto
    override var showGraphValueDescriptions: Boolean = true
    override var shouldCombineCT2WithPVPower: Boolean = true
    override var shouldCombineCT2WithLoadsPower: Boolean = false
    override var currencyCode: String = "GBP"
    override var feedInUnitPrice: Double = 0.05
    override var gridImportUnitPrice: Double = 0.15
    override var currencySymbol: String = "£"
    override val variables: List<Variable>
        get() = listOf()
    override var showUsableBatteryOnly: Boolean = false
    override val themeStream: MutableStateFlow<AppTheme> = MutableStateFlow(AppTheme.preview())
    override var decimalPlaces: Int = 3
    override var showSunnyBackground: Boolean = true
    override var selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode = SelfSufficiencyEstimateMode.Off
    override var showBatteryEstimate: Boolean = false
    override var showBatteryTemperature: Boolean = false
    override var showFinancialSummary: Boolean = true
    override var useLargeDisplay: Boolean = false
    override val minSOC: MutableStateFlow<Double?> = MutableStateFlow(20.0)
    override var batteryCapacity: Int = 3000
    override var isDemoUser: Boolean = true
    override var useColouredFlowLines: Boolean = true
    override var refreshFrequency: RefreshFrequency = RefreshFrequency.Auto
    override var displayUnit = DisplayUnit.Kilowatts
    override var showInverterTemperatures: Boolean = true
    override var selectedParameterGraphVariables: List<String> = listOf()
    override var showInverterIcon: Boolean = true
    override var showHomeTotal: Boolean = true
    override var shouldInvertCT2: Boolean = false
    override var showGridTotals: Boolean = false
    override var showInverterStationNameOnPowerflow: Boolean = false
    override var showInverterTypeNameOnPowerflow: Boolean = false
    override var showLastUpdateTimestamp: Boolean = false
    override var solarRangeDefinitions: SolarRangeDefinitions = SolarRangeDefinitions.defaults
    override var parameterGroups: List<ParameterGroup> = ParameterGroup.defaults
    override var solcastSettings: SolcastSettings = SolcastSettings(apiKey = null, sites = listOf(SolcastSite.preview()))
    override var dataCeiling: DataCeiling = DataCeiling.Mild
    override var totalYieldModel: TotalYieldModel = TotalYieldModel.Off
    override var showFinancialSummaryOnFlowPage: Boolean = false
    override var separateParameterGraphsByUnit: Boolean = true
    override var showBatteryAsPercentage: Boolean = false
    override var showSeparateStringsOnPowerFlow: Boolean = false
    override var useExperimentalLoadFormula: Boolean = false

    override var devices: List<Device>? = listOf(
        Device(
            deviceSN = "123123",
            stationName = "my station",
            stationID = "station1",
            battery = Battery("1200", "20", false),
            moduleSN = "module123",
            hasPV = true,
            hasBattery = true,
            deviceType = "F1-3000"
        ),
        Device(
            deviceSN = "123123",
            stationName = "my station",
            stationID = "station2",
            battery = Battery("1200", "20", false),
            moduleSN = "module123",
            hasPV = true,
            hasBattery = true,
            deviceType = "H1-5A"
        )
    )
    override var currentDevice: MutableStateFlow<Device?> = MutableStateFlow(
        Device(
            deviceSN = "123123",
            stationName = "my station",
            stationID = "station1",
            battery = Battery("1200", "20", false),
            moduleSN = "module123",
            hasPV = true,
            hasBattery = true,
            deviceType = "F1-3000"
        )
    )
    override val selectedDeviceSN: String? = "f3000_deviceid"

    override fun logout(clearDisplaySettings: Boolean, clearDeviceSettings: Boolean) {
    }

    override suspend fun fetchDevices() {
    }

    override fun select(device: Device) {
    }

    override var appVersion: String = "1.29"
}