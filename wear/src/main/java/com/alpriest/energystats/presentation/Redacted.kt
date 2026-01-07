package com.alpriest.energystats.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.rememberPlaceholderState
import com.alpriest.energystats.shared.helpers.asPercent
import com.alpriest.energystats.shared.helpers.kW

@Composable
@OptIn(ExperimentalWearMaterialApi::class)
fun RedactedKW(amount: Double?, textStyle: TextStyle) {
    val placeholderState = rememberPlaceholderState { amount != null }

    Text(
        text = (amount ?: 8.8).kW(2),
        modifier = Modifier.placeholder(placeholderState),
        style = textStyle
    )
}

@Composable
@OptIn(ExperimentalWearMaterialApi::class)
fun RedactedPercentage(amount: Double?, textStyle: TextStyle) {
    val placeholderState = rememberPlaceholderState { amount != null }

    Text(
        text = (amount ?: 8.8).asPercent(),
        modifier = Modifier.placeholder(placeholderState),
        style = textStyle
    )
}