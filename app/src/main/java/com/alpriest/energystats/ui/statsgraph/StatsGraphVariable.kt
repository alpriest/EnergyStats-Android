package com.alpriest.energystats.ui.statsgraph

import com.alpriest.energystats.models.ReportVariable

data class StatsGraphVariable(
    val type: ReportVariable,
    var enabled: Boolean,
)
