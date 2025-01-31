package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.SegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SelfSufficiencySettingsView(config: ConfigManaging, modifier: Modifier = Modifier) {
    val selfSufficiencyEstimateModeState = rememberSaveable { mutableStateOf(config.selfSufficiencyEstimateMode) }
    val showSelfSufficiencyStatsGraphOverlayState = rememberSaveable { mutableStateOf(config.showSelfSufficiencyStatsGraphOverlay) }
    val description: String? = when (selfSufficiencyEstimateModeState.value) {
        SelfSufficiencyEstimateMode.Absolute -> stringResource(R.string.absolute_self_sufficiency)
        SelfSufficiencyEstimateMode.Net -> stringResource(R.string.net_self_sufficiency)
        else -> stringResource(R.string.self_sufficiency_off)
    }
    val context = LocalContext.current
    trackScreenView("Self sufficiency estimates", "SelfSufficiencySettingsView")

    SettingsColumn(
        modifier = modifier.fillMaxWidth(),
        padding = PaddingValues(10.dp),
        footer = description
    ) {
        SettingsSegmentedControl(title = null, segmentedControl = {
            val items = listOf(SelfSufficiencyEstimateMode.Off, SelfSufficiencyEstimateMode.Net, SelfSufficiencyEstimateMode.Absolute)
            SegmentedControl(
                items = items.map { it.title(context) },
                defaultSelectedItemIndex = items.indexOf(selfSufficiencyEstimateModeState.value),
                color = colorScheme.primary
            ) {
                selfSufficiencyEstimateModeState.value = items[it]
                config.selfSufficiencyEstimateMode = items[it]
            }
        })
    }

    if (selfSufficiencyEstimateModeState.value != SelfSufficiencyEstimateMode.Off) {
        SettingsColumn {
            SettingsCheckbox(
                title = stringResource(R.string.show_self_sufficiency_percentage_on_stats_graph),
                state = showSelfSufficiencyStatsGraphOverlayState,
                onUpdate = {
                    config.showSelfSufficiencyStatsGraphOverlay = it
                }
            )
        }
    }
}

@Preview(
    showBackground = false,
    heightDp = 640
)
@Composable
fun SelfSufficiencySettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        SelfSufficiencySettingsView(config = FakeConfigManager())
    }
}