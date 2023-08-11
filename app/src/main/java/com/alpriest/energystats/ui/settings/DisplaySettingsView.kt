package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

enum class SelfSufficiencyEstimateMode(val value: Int) {
    Off(0),
    Net(1),
    Absolute(2);

    fun title(): String {
        return when (this) {
            Net -> "Net"
            Absolute -> "Absolute"
            else -> "Off"
        }
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

@Composable
fun DisplaySettingsView(config: ConfigManaging, modifier: Modifier = Modifier) {
    val largeDisplayState = rememberSaveable { mutableStateOf(config.useLargeDisplay) }
    val colouredFlowLinesState = rememberSaveable { mutableStateOf(config.useColouredFlowLines) }
    val showSunnyBackgroundState = rememberSaveable { mutableStateOf(config.showSunnyBackground) }
    val decimalPlacesState = rememberSaveable { mutableStateOf(config.decimalPlaces) }
    val showTotalYieldState = rememberSaveable { mutableStateOf(config.showTotalYield) }
    val showEstimatedEarningsState = rememberSaveable { mutableStateOf(config.showEstimatedEarnings) }

    SettingsColumnWithChild(
        modifier = modifier
    ) {
        SettingsTitleView(stringResource(R.string.display))

        SettingsCheckbox(
            title = stringResource(R.string.increase_sizes_for_large_display),
            state = largeDisplayState,
            onConfigUpdate = { config.useLargeDisplay = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_coloured_flow_lines),
            state = colouredFlowLinesState,
            onConfigUpdate = { config.useColouredFlowLines = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_total_yield),
            state = showTotalYieldState,
            onConfigUpdate = { config.showTotalYield = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_sunny_background),
            state = showSunnyBackgroundState,
            onConfigUpdate = { config.showSunnyBackground = it }
        )

        SettingsCheckbox(
            title = "Show estimated earnings",
            state = showEstimatedEarningsState,
            onConfigUpdate = { config.showEstimatedEarnings = it }
        )

        Text(
            buildAnnotatedString {
                append("Shows earnings today, this month, this year, and all-time based on a crude calculation of")
                append(" ")
                withStyle(
                    style = SpanStyle(fontStyle = FontStyle.Italic, color = colors.onSecondary)
                ) {
                    append("feed-in kWh * price per kWh")
                }
                append(" ")
                append("as configured on FoxESS cloud.")
            },
            modifier = Modifier.padding(start = 48.dp),
            color = colors.onSecondary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.decimal_places),
                color = colors.onSecondary,
            )
            listOf(2, 3).map {
                RadioButton(
                    selected = decimalPlacesState.value == it,
                    onClick = {
                        decimalPlacesState.value = it
                        config.decimalPlaces = it
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                )
                Text(
                    it.toString(),
                    color = colors.onSecondary,
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
fun DisplaySettingsViewPreview() {
    EnergyStatsTheme(darkTheme = false) {
        DisplaySettingsView(config = FakeConfigManager(), modifier = Modifier.padding(horizontal = 12.dp))
    }
}