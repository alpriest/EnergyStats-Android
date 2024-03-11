package com.alpriest.energystats.ui.settings

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.models.w
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.SegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun DataSettingsView(config: ConfigManaging) {
    val displayUnitState = rememberSaveable { mutableStateOf(config.displayUnit) }
    val dataCeilingState = rememberSaveable { mutableStateOf(config.dataCeiling) }
    val useTraditionalLoadFormulaState = rememberSaveable { mutableStateOf(config.useTraditionalLoadFormula) }
    val context = LocalContext.current

    SettingsColumn(
        header = "Data"
    ) {
        SettingsSegmentedControl(
            title = stringResource(R.string.units),
            segmentedControl = {
                val items = listOf(
                    DisplayUnit.Watts,
                    DisplayUnit.Kilowatts,
                    DisplayUnit.Adaptive
                )
                SegmentedControl(
                    items = items.map { it.title(context) },
                    defaultSelectedItemIndex = items.indexOf(displayUnitState.value),
                    color = MaterialTheme.colors.primary
                ) {
                    displayUnitState.value = items[it]
                    config.displayUnit = items[it]
                }
            },
            footer = buildAnnotatedString {
                when (displayUnitState.value) {
                    DisplayUnit.Kilowatts -> append(
                        stringResource(
                            R.string.display_unit_kilowatts_description,
                            3.456.kW(config.decimalPlaces),
                            0.123.kW(config.decimalPlaces)
                        )
                    )

                    DisplayUnit.Watts -> append(stringResource(R.string.display_unit_watts_description, 3.456.w(), 0.123.w()))
                    DisplayUnit.Adaptive -> append(stringResource(R.string.display_unit_adaptive_description, 3.456.kW(config.decimalPlaces), 0.123.w()))
                }
            }
        )
    }

    SettingsColumn {
        SettingsSegmentedControl(
            title = stringResource(R.string.data_ceiling),
            segmentedControl = {
                val items = listOf(DataCeiling.None, DataCeiling.Mild, DataCeiling.Enhanced)
                SegmentedControl(
                    items = items.map { it.title(context) },
                    defaultSelectedItemIndex = items.indexOf(dataCeilingState.value),
                    color = MaterialTheme.colors.primary
                ) {
                    dataCeilingState.value = items[it]
                    config.dataCeiling = items[it]
                }
            },
            footer = buildAnnotatedString {
                when (dataCeilingState.value) {
                    DataCeiling.None -> append(stringResource(R.string.data_ceiling_none_description))
                    DataCeiling.Mild -> append(stringResource(R.string.data_ceiling_mild_description))
                    DataCeiling.Enhanced -> append(stringResource(R.string.data_ceiling_enhanced_description))
                }
            }
        )
    }

    RefreshFrequencySettingsView(config)

    SettingsColumn {
        SettingsCheckbox(
            title = "Use traditional load formula",
            state = useTraditionalLoadFormulaState,
            onUpdate = { config.useTraditionalLoadFormula = !it },
            footer = buildAnnotatedString {
                append("Uses the FoxESS loads value to show load which doesn't handle +ve/-ve CT2 very well. Changes only take effect on next data fetch.")
            }
        )
    }
}

@Preview(showBackground = true, heightDp = 940)
@Composable
fun DataSettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SettingsPage {
            DataSettingsView(
                config = FakeConfigManager()
            )
        }
    }
}