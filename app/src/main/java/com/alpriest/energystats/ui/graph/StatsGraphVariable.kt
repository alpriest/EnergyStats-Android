package com.alpriest.energystats.ui.graph

import com.alpriest.energystats.models.ReportVariable

data class StatsGraphVariable(
    val type: ReportVariable,
    var enabled: Boolean,
)
