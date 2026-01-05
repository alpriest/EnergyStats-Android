package com.alpriest.energystats.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun EnergyStatsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}