package com.alpriest.energystats.preview

import com.alpriest.energystats.models.Battery
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.DeviceFirmwareVersion
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.FinancialModel
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.settings.solcast.SolcastSettings
import com.alpriest.energystats.ui.settings.solcast.SolcastSite
import com.alpriest.energystats.ui.settings.solcast.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConfigManager : ConfigManaging {
    override var colorThemeMode: ColorThemeMode = ColorThemeMode.Auto
    override var showGraphValueDescriptions: Boolean = true
    override var shouldCombineCT2WithPVPower: Boolean = true
    override var currencyCode: String = "GBP"
    override var feedInUnitPrice: Double = 0.05
    override var gridImportUnitPrice: Double = 0.15
    override var currencySymbol: String = "£"
    override var financialModel: FinancialModel = FinancialModel.EnergyStats
    override val variables: List<RawVariable>
        get() = listOf()
    override val hasBattery: Boolean
        get() = true
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
    override var showInverterPlantNameOnPowerflow: Boolean = false
    override var showInverterTypeNameOnPowerflow: Boolean = false
    override var showLastUpdateTimestamp: Boolean = false
    override var solarRangeDefinitions: SolarRangeDefinitions = SolarRangeDefinitions.defaults
    override var parameterGroups: List<ParameterGroup> = ParameterGroup.defaults
    override var solcastSettings: SolcastSettings = SolcastSettings(apiKey = null, sites = listOf(SolcastSite.preview()))
    override var dataCeiling: DataCeiling = DataCeiling.Mild
    override var totalYieldModel: TotalYieldModel = TotalYieldModel.Off

    override var devices: List<Device>? = listOf(
        Device(
            plantName = "plant 1",
            deviceID = "f3000_deviceid",
            deviceSN = "123123",
            battery = Battery("1200", "20"),
            hasPV = true,
            hasBattery = true,
            deviceType = "F3000",
            firmware = DeviceFirmwareVersion("1.50", "1.02", "1.20"),
            variables = listOf(),
            moduleSN = "module123"
        ),
        Device(
            plantName = "plant 2",
            deviceID = "f3-5_deviceid",
            deviceSN = "123123",
            battery = Battery("1200", "20"),
            hasPV = true,
            hasBattery = true,
            deviceType = "f3-5",
            firmware = DeviceFirmwareVersion("1.50", "1.02", "1.20"),
            variables = listOf(),
            moduleSN = "module123"
        )
    )
    override var currentDevice: MutableStateFlow<Device?> = MutableStateFlow(
        Device(
            plantName = "plant 1",
            deviceID = "f3000_deviceid",
            deviceSN = "123123",
            battery = Battery("1200", "20"),
            hasPV = true,
            hasBattery = true,
            deviceType = "F3000",
            firmware = DeviceFirmwareVersion("1.50", "1.02", "1.20"),
            variables = listOf(),
            moduleSN = "module123"
        )
    )
    override val selectedDeviceID: String? = "f3000_deviceid"

    override fun logout() {
    }

    override suspend fun fetchDevices() {
    }

    override suspend fun refreshFirmwareVersions() {
    }

    override fun select(device: Device) {
    }

    override var appVersion: String = "1.29"
}