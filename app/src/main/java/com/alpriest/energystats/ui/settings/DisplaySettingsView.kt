package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.battery.SettingsTitleView
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

    SettingsColumnWithChild(
        modifier = modifier
    ) {
        SettingsTitleView(stringResource(R.string.display))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    largeDisplayState.value = !largeDisplayState.value
                    config.useLargeDisplay = largeDisplayState.value
                }
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = largeDisplayState.value,
                onCheckedChange = {
                    largeDisplayState.value = it
                    config.useLargeDisplay = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )
            Text(
                stringResource(R.string.increase_sizes_for_large_display),
                color = colors.onSecondary,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    colouredFlowLinesState.value = !colouredFlowLinesState.value
                    config.useColouredFlowLines = colouredFlowLinesState.value
                }
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = colouredFlowLinesState.value,
                onCheckedChange = {
                    colouredFlowLinesState.value = it
                    config.useColouredFlowLines = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )
            Text(
                stringResource(R.string.show_coloured_flow_lines),
                color = colors.onSecondary,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    showTotalYieldState.value = !showTotalYieldState.value
                    config.showTotalYield = showTotalYieldState.value
                }
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = showTotalYieldState.value,
                onCheckedChange = {
                    showTotalYieldState.value = it
                    config.showTotalYield = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )
            Text(
                stringResource(R.string.show_total_yield),
                color = colors.onSecondary,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    showSunnyBackgroundState.value = !showSunnyBackgroundState.value
                    config.showSunnyBackground = showSunnyBackgroundState.value
                }
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = showSunnyBackgroundState.value,
                onCheckedChange = {
                    showSunnyBackgroundState.value = it
                    config.showSunnyBackground = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )
            Text(
                stringResource(R.string.show_sunny_background),
                color = colors.onSecondary,
            )
        }

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

@Preview(
    showBackground = true,
    heightDp = 640
)
@Composable
fun DisplaySettingsViewPreview() {
    EnergyStatsTheme(darkTheme = false) {
        DisplaySettingsView(config = FakeConfigManager(), modifier = Modifier.padding(horizontal = 12.dp))
    }
}