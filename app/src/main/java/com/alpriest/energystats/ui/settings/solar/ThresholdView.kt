package com.alpriest.energystats.ui.settings.solar

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPaddingValues

@Composable
fun ThresholdView(value: Float, title: String, description: String, onChange: (Float) -> Unit) {
    SettingsColumn(
        padding = SettingsPaddingValues.Companion.withVertical(),
        header = title
    ) {
        Row {
            Slider(
                value = value,
                onValueChange = { onChange(it) },
                valueRange = 0.1f..10.0f,
                modifier = Modifier.Companion.weight(1.0f),
                steps = 48,
                colors = SliderDefaults.colors(
                    activeTickColor = MaterialTheme.colorScheme.primary,
                    inactiveTickColor = MaterialTheme.colorScheme.background,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.background,
                    thumbColor = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                value.toDouble().kW(3),
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.weight(0.2f),
                color = MaterialTheme.colorScheme.onSecondary
            )
        }

        Text(
            description,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}