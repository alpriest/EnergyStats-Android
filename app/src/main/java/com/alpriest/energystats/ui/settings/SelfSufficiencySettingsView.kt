package com.alpriest.energystats.ui.settings

import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.SegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SelfSufficiencySettingsView(config: ConfigManaging, modifier: Modifier = Modifier) {
    val selfSufficiencyEstimateModeState = rememberSaveable { mutableStateOf(config.selfSufficiencyEstimateMode) }
    val description: String? = when (selfSufficiencyEstimateModeState.value) {
        SelfSufficiencyEstimateMode.Absolute -> stringResource(R.string.absolute_self_sufficiency)
        SelfSufficiencyEstimateMode.Net -> stringResource(R.string.net_self_sufficiency)
        else -> null
    }
    val context = LocalContext.current

    SettingsColumnWithChild(
        modifier = modifier
    ) {
        SettingsTitleView(stringResource(R.string.self_sufficiency_estimates))

        SettingsSegmentedControl(title = null, segmentedControl = {
            val items = listOf(SelfSufficiencyEstimateMode.Off, SelfSufficiencyEstimateMode.Net, SelfSufficiencyEstimateMode.Absolute)
            SegmentedControl(
                items = items.map { it.title(context) },
                defaultSelectedItemIndex = items.indexOf(selfSufficiencyEstimateModeState.value),
                color = colors.primary
            ) {
                selfSufficiencyEstimateModeState.value = items[it]
                config.selfSufficiencyEstimateMode = items[it]
            }
        }, footer = buildAnnotatedString {
            description?.let {
                append(it)
            }
        })
    }
}

@Preview(
    showBackground = true,
    heightDp = 640
)
@Composable
fun SelfSufficiencySettingsViewPreview() {
    EnergyStatsTheme {
        SelfSufficiencySettingsView(config = FakeConfigManager())
    }
}