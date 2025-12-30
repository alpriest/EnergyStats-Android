package com.alpriest.energystats.ui.settings

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.helpers.kW
import com.alpriest.energystats.shared.helpers.w
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.helpers.SegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun DataSettingsView(config: ConfigManaging, modifier: Modifier) {
    val displayUnitState = rememberSaveable { mutableStateOf(config.displayUnit) }
    val dataCeilingState = rememberSaveable { mutableStateOf(config.dataCeiling) }
    val useTraditionalLoadFormulaState = rememberSaveable { mutableStateOf(config.useTraditionalLoadFormula) }
    val context = LocalContext.current
    trackScreenView("Data", "DataSettingsView")

    SettingsPage(modifier) {
        SettingsColumn {
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
                        color = colorScheme.primary
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
                        color = colorScheme.primary
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
                title = stringResource(R.string.use_traditional_load_formula),
                state = useTraditionalLoadFormulaState,
                onUpdate = { config.useTraditionalLoadFormula = !it },
                footer = buildAnnotatedString {
                    append(stringResource(R.string.use_traditional_load_formula_description))
                }
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 940)
@Composable
fun DataSettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SettingsPage(Modifier) {
            DataSettingsView(
                config = FakeConfigManager(),
                Modifier
            )
        }
    }
}