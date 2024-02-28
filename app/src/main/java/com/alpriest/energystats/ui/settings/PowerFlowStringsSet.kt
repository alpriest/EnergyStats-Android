package com.alpriest.energystats.ui.settings

import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.ui.flow.StringPower
import com.alpriest.energystats.ui.flow.currentValue
import java.util.EnumSet

enum class PowerFlowStrings {
    PV1, PV2, PV3, PV4;
}

class PowerFlowStringsSet(powerFlowStringsSet: EnumSet<PowerFlowStrings>) {
    var optionSet: EnumSet<PowerFlowStrings> = powerFlowStringsSet

    private fun add(option: PowerFlowStrings) {
        optionSet.add(option)
    }

    private fun remove(option: PowerFlowStrings) {
        optionSet.remove(option)
    }

    fun contains(option: PowerFlowStrings): Boolean = optionSet.contains(option)

    fun variableNames(): Set<String> {
        var variables: MutableSet<String> = mutableSetOf()

        if (contains(PowerFlowStrings.PV1)) {
            variables.add("pv1Power")
        }

        if (contains(PowerFlowStrings.PV2)) {
            variables.add("pv2Power")
        }

        if (contains(PowerFlowStrings.PV3)) {
            variables.add("pv3Power")
        }

        if (contains(PowerFlowStrings.PV4)) {
            variables.add("pv4Power")
        }

        return variables
    }

    fun makeStringPowers(response: OpenQueryResponse): List<StringPower> {
        val strings: MutableList<StringPower> = mutableListOf()

        if (contains(PowerFlowStrings.PV1)) {
            strings.add(StringPower("PV1", response.datas.currentValue("pv1Power")))
        }

        if (contains(PowerFlowStrings.PV2)) {
            strings.add(StringPower("PV2", response.datas.currentValue("pv2Power")))
        }

        if (contains(PowerFlowStrings.PV3)) {
            strings.add(StringPower("PV3", response.datas.currentValue("pv3Power")))
        }

        if (contains(PowerFlowStrings.PV4)) {
            strings.add(StringPower("PV4", response.datas.currentValue("pv4Power")))
        }

        return strings
    }

    fun toggle(value: PowerFlowStrings) {
        if (contains(value)) {
            remove(value)
        } else {
            add(value)
        }
    }

    companion object {
        val defaults: PowerFlowStringsSet
            get() {
                return PowerFlowStringsSet(EnumSet.noneOf(PowerFlowStrings::class.java))
            }
    }
}
