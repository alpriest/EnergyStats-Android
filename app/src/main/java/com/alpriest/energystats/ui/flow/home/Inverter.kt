package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun InverterSpacer(modifier: Modifier = Modifier, themeStream: MutableStateFlow<AppTheme>) {
    val strokeWidth = themeStream.collectAsState().value.strokeWidth()

    Canvas(modifier) {
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, strokeWidth / 2f),
            end = Offset(size.width, strokeWidth / 2f),
            strokeWidth = strokeWidth
        )
    }
}
