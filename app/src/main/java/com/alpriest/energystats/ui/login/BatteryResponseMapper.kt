package com.alpriest.energystats.ui.login

import com.alpriest.energystats.shared.models.Battery
import com.alpriest.energystats.shared.models.network.BatterySOCResponse
import com.alpriest.energystats.shared.models.network.OpenRealQueryResponse
import com.alpriest.energystats.shared.models.network.SoC
import com.alpriest.energystats.shared.models.network.currentData

enum class BatteryResponseMapper {
    ;

    companion object {
        fun map(batteryVariables: OpenRealQueryResponse, settings: BatterySOCResponse): Battery {
            val residualData = batteryVariables.datas.currentData("ResidualEnergy")
            val residual = residualData?.value ?: 0.0

            var unitWh = 10.0
            residualData?.unit?.trim()?.let {
                unitWh = when {
                    it.endsWith("kWh") -> it.removeSuffix("kWh").trim().toDouble() * 1000.0
                    it.endsWith("Wh") -> it.removeSuffix("Wh").trim().toDouble()
                    else -> unitWh
                }
            }

            val soc = batteryVariables.datas.SoC()

            val batteryCapacity: String = if (soc > 0) {
                ((residual * unitWh) / (soc / 100.0)).toInt().toString()
            } else {
                "0"
            }

            val minSOC: String = (settings.minSocOnGrid.toDouble() / 100.0).toString()

            return Battery(capacity = batteryCapacity, minSOC = minSOC, hasError = false)
        }
    }
}
