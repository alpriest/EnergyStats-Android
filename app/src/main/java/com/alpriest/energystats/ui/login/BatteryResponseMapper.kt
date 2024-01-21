package com.alpriest.energystats.ui.login

import com.alpriest.energystats.models.Battery
import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.ui.flow.currentValue

enum class BatteryResponseMapper {
    ;
    companion object {
        fun map(batteryVariables: OpenQueryResponse, settings: BatterySOCResponse): Battery? {
            val residual = batteryVariables.datas.currentValue("ResidualEnergy")
            val soc = batteryVariables.datas.currentValue("SoC")

            val batteryCapacity: String = if (soc > 0) {
                ((residual * 10.0) / (soc / 100.0)).toInt().toString()
            } else {
                "0"
            }
            val minSOC: String = (settings.minSocOnGrid.toDouble() / 100.0).toString()

            return Battery(capacity = batteryCapacity, minSOC = minSOC, hasError = false)
        }
    }
}
