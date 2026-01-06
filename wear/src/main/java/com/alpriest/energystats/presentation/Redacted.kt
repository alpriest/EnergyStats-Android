package com.alpriest.energystats.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.rememberPlaceholderState
import com.alpriest.energystats.shared.helpers.asPercent
import com.alpriest.energystats.shared.helpers.kW

@Composable
@OptIn(ExperimentalWearMaterialApi::class)
fun RedactedKW(amount: Double?) {
    val placeholderState = rememberPlaceholderState { amount != null }

    Text(
        text = (amount ?: 8.8).kW(2),
        modifier = Modifier.Companion.placeholder(placeholderState)
    )
}

@Composable
@OptIn(ExperimentalWearMaterialApi::class)
fun RedactedPercentage(amount: Double?) {
    val placeholderState = rememberPlaceholderState { amount != null }

    Text(
        text = (amount ?: 8.8).asPercent(),
        modifier = Modifier.Companion.placeholder(placeholderState)
    )
}