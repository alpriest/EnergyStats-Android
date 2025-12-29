package com.alpriest.energystats.ui.settings

import com.alpriest.energystats.shared.models.network.OpenRealQueryResponse
import com.alpriest.energystats.ui.flow.StringPower
import com.alpriest.energystats.ui.flow.currentValue
import kotlin.math.abs

data class PowerFlowStringsSettings(
    val enabled: Boolean = false,
    val pv1Name: String = "PV1",
    val pv1Enabled: Boolean = false,
    val pv2Name: String = "PV2",
    val pv2Enabled: Boolean = false,
    val pv3Name: String = "PV3",
    val pv3Enabled: Boolean = false,
    val pv4Name: String = "PV4",
    val pv4Enabled: Boolean = false,
    val pv5Name: String = "PV5",
    val pv5Enabled: Boolean = false,
    val pv6Name: String = "PV6",
    val pv6Enabled: Boolean = false
) {
    fun variableNames(): Set<String> {
        val variables: MutableSet<String> = mutableSetOf()

        if (pv1Enabled) {
            variables.add("pv1Power")
        }

        if (pv2Enabled) {
            variables.add("pv2Power")
        }

        if (pv3Enabled) {
            variables.add("pv3Power")
        }

        if (pv4Enabled) {
            variables.add("pv4Power")
        }

        if (pv5Enabled) {
            variables.add("pv5Power")
        }

        if (pv6Enabled) {
            variables.add("pv6Power")
        }

        return variables
    }

    fun makeStringPowers(response: OpenRealQueryResponse): List<StringPower> {
        val strings: MutableList<StringPower> = mutableListOf()

        if (pv1Enabled) {
            strings.add(StringPower("PV1", abs(response.datas.currentValue("pv1Power"))))
        }

        if (pv2Enabled) {
            strings.add(StringPower("PV2", abs(response.datas.currentValue("pv2Power"))))
        }

        if (pv3Enabled) {
            strings.add(StringPower("PV3", abs(response.datas.currentValue("pv3Power"))))
        }

        if (pv4Enabled) {
            strings.add(StringPower("PV4", abs(response.datas.currentValue("pv4Power"))))
        }

        if (pv5Enabled) {
            strings.add(StringPower("PV5", abs(response.datas.currentValue("pv5Power"))))
        }

        if (pv6Enabled) {
            strings.add(StringPower("PV6", abs(response.datas.currentValue("pv6Power"))))
        }

        return strings
    }

    fun copy(
        enabled: Boolean? = null,
        pv1Name: String? = null,
        pv1Enabled: Boolean? = null,
        pv2Name: String? = null,
        pv2Enabled: Boolean? = null,
        pv3Name: String? = null,
        pv3Enabled: Boolean? = null,
        pv4Name: String? = null,
        pv4Enabled: Boolean? = null,
        pv5Name: String? = null,
        pv5Enabled: Boolean? = null,
        pv6Name: String? = null,
        pv6Enabled: Boolean? = null
    ): PowerFlowStringsSettings {
        return PowerFlowStringsSettings(
            enabled = enabled ?: this.enabled,
            pv1Name = pv1Name ?: this.pv1Name,
            pv1Enabled = pv1Enabled ?: this.pv1Enabled,
            pv2Name = pv2Name ?: this.pv2Name,
            pv2Enabled = pv2Enabled ?: this.pv2Enabled,
            pv3Name = pv3Name ?: this.pv3Name,
            pv3Enabled = pv3Enabled ?: this.pv3Enabled,
            pv4Name = pv4Name ?: this.pv4Name,
            pv4Enabled = pv4Enabled ?: this.pv4Enabled,
            pv5Name = pv5Name ?: this.pv5Name,
            pv5Enabled = pv5Enabled ?: this.pv5Enabled,
            pv6Name = pv6Name ?: this.pv6Name,
            pv6Enabled = pv6Enabled ?: this.pv6Enabled
        )
    }

    companion object {
        val defaults: PowerFlowStringsSettings
            get() {
                return PowerFlowStringsSettings()
            }
    }
}
