package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.Variable
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime

data class ParametersDisplayMode(
    val date: LocalDate,
    val hours: Int
)

data class ParametersGraphValue(val graphPoint: Int, val time: LocalDateTime, val value: Double, val type: Variable)

data class ParameterGraphVariable(
    val type: Variable,
    val isSelected: Boolean,
    override val enabled: Boolean
) : GraphVariable {
    @Composable
    override fun colour(appSettingsStream: StateFlow<AppSettings>): Color {
        return type.colour()
    }
}

data class ParameterGraphBounds(
    val type: Variable,
    val min: Float,
    val max: Float,
    val now: Float
)

interface GraphVariable {
    val enabled: Boolean

    @Composable
    fun colour(appSettingsStream: StateFlow<AppSettings>): Color
}