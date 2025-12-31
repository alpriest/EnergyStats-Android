package com.alpriest.energystats.shared.models

data class ParameterGroup(val id: String, val title: String, val parameterNames: List<String>) {
    companion object {
        val defaults: List<ParameterGroup>
            get() {
                return listOf(
                    ParameterGroup(
                        id = "5875390f-62d0-4373-909c-87225ad5150c",
                        "Compare strings", listOf(
                            "pv1Power",
                            "pv2Power",
                            "pv3Power",
                            "pv4Power"
                        )
                    ),
                    ParameterGroup(
                        id = "ae0a66a8-d8ba-463b-8c58-c671ba130dc9",
                        "Temperatures", listOf(
                            "ambientTemperation",
                            "boostTemperation",
                            "invTemperation",
                            "chargeTemperature",
                            "batTemperature",
                            "dspTemperature"
                        )
                    ),
                    ParameterGroup(
                        id = "f633b777-29c5-4809-a783-6038e055cc86",
                        "Battery", listOf(
                            "batTemperature",
                            "batVolt",
                            "batCurrent",
                            "SoC"
                        )
                    ),
                )
            }
    }
}