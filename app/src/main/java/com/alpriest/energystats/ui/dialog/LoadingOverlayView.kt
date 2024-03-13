package com.alpriest.energystats.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun LoadingOverlayView() {
    Card(
        border = BorderStroke(1.dp, Color.Gray),
        shape = RectangleShape
    ) {
        Text(
            stringResource(R.string.loading),
            color = colors.onSecondary,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview
@Composable
fun LoadingOverlayPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        LoadingOverlayView()
    }
}