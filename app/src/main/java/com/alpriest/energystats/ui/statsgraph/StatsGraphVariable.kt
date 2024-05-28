package com.alpriest.energystats.ui.statsgraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.models.ReportVariable

data class StatsGraphVariable(
    val type: ReportVariable,
    override var enabled: Boolean,
): GraphVariable {
    @Composable
    override fun colour(): Color {
        return type.colour()
    }
}
