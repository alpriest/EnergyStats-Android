package com.alpriest.energystats.shared.models

enum class ValueUsage {
    SNAPSHOT,
    TOTAL;
}

enum class ReportVariable {
    FeedIn,
    Generation,
    GridConsumption,
    ChargeEnergyToTal,
    DischargeEnergyToTal,
    Loads,
    SelfSufficiency,
    PvEnergyToTal,
    InverterConsumption,
    BatterySOC;

    fun networkTitle(): String {
        return when (this) {
            FeedIn -> "feedin"
            Generation -> "generation"
            GridConsumption -> "gridConsumption"
            ChargeEnergyToTal -> "chargeEnergyToTal"
            DischargeEnergyToTal -> "dischargeEnergyToTal"
            Loads -> "loads"
            SelfSufficiency -> "selfSufficiency"
            PvEnergyToTal -> "PVEnergyTotal"
            InverterConsumption -> "InverterConsumption"
            BatterySOC -> "BatterySOC"
        }
    }

    companion object
}

fun ReportVariable.Companion.parse(networkVariable: String): ReportVariable {
    return when (networkVariable.lowercase()) {
        "generation" -> ReportVariable.Generation
        "gridconsumption" -> ReportVariable.GridConsumption
        "chargeenergytotal" -> ReportVariable.ChargeEnergyToTal
        "dischargeenergytotal" -> ReportVariable.DischargeEnergyToTal
        "loads" -> ReportVariable.Loads
        "pvenergytotal" -> ReportVariable.PvEnergyToTal
        else -> {
            ReportVariable.FeedIn
        }
    }
}
