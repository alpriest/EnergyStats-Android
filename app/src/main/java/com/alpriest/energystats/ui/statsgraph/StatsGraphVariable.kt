package com.alpriest.energystats.ui.statsgraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

data class StatsGraphVariable(
    val type: ReportVariable,
    override var enabled: Boolean,
): GraphVariable {
    @Composable
    override fun colour(themeStream: MutableStateFlow<AppTheme>): Color {
        return type.colour(themeStream)
    }
}
