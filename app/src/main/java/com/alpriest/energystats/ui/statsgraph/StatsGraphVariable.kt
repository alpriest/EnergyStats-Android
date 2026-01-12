package com.alpriest.energystats.ui.statsgraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.models.colour
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.ui.paramsgraph.GraphVariable
import com.alpriest.energystats.shared.models.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow

data class StatsGraphVariable(
    val type: ReportVariable,
    override var enabled: Boolean,
) : GraphVariable {
    @Composable
    override fun colour(themeStream: MutableStateFlow<AppSettings>): Color {
        return type.colour(themeStream)
    }
}
