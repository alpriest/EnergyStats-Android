package com.alpriest.energystats.ui.paramsgraph.graphs

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable
import kotlinx.coroutines.flow.StateFlow

@Composable
fun colorsForVariables(graphVariables: List<ParameterGraphVariable>, appSettingsStream: StateFlow<AppSettings>): Map<String, List<Color>> {
    return graphVariables
        .filter { it.isSelected }
        .groupBy { it.type.unit }
        .mapValues { (_, varsForUnit) ->
            varsForUnit.map { variable ->
                if (variable.enabled) {
                    variable.type.colour(appSettingsStream)
                } else {
                    Color.Transparent
                }
            }
        }
}