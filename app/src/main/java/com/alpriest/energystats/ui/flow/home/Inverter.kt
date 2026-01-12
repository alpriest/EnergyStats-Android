package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.shared.models.AppSettings
import kotlinx.coroutines.flow.StateFlow

@Composable
fun InverterSpacer(modifier: Modifier = Modifier, appSettingsStream: StateFlow<AppSettings>) {
    val strokeWidth = appSettingsStream.collectAsState().value.strokeWidth()

    Canvas(modifier) {
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, strokeWidth / 2f),
            end = Offset(size.width, strokeWidth / 2f),
            strokeWidth = strokeWidth
        )
    }
}
