package com.alpriest.energystats.models

data class CalculationBreakdown(val formula: String, val calculation: (Int) -> String)