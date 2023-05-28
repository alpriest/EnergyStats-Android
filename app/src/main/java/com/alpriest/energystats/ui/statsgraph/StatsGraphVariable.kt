package com.alpriest.energystats.ui.statsgraph

import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.models.ReportVariable

data class StatsGraphVariable(
    val type: ReportVariable,
    override var enabled: Boolean,
): GraphVariable {
    override val colour: Color
        get() = type.colour()
}
