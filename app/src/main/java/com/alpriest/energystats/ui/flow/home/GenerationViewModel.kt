package com.alpriest.energystats.ui.flow.home

import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.ui.settings.TotalYieldModel

class GenerationViewModel(private val raws: List<RawResponse>, private val todayGeneration: Double) {
    fun todayGeneration(model: TotalYieldModel): Double {
        return when (model) {
            TotalYieldModel.Off -> 0.0
            TotalYieldModel.EnergyStats -> calculateSolar(raws)
            TotalYieldModel.FoxESS -> todayGeneration
        }
    }

    private fun calculateSolar(raws: List<RawResponse>): Double {
        val filteredVariables = raws.filter { it.variable == "pvPower" }

        val totalSum = filteredVariables.flatMap { it.data.toList() }.sumOf { it.value }

        return totalSum / 12.0
    }
}