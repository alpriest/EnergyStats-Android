package com.alpriest.energystats.ui.settings

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.RefreshFrequency
import com.alpriest.energystats.ui.helpers.SegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun RefreshFrequencySettingsView(config: ConfigManaging) {
    val refreshFrequency = rememberSaveable { mutableStateOf(config.refreshFrequency) }

    SettingsColumnWithChild {
        SettingsSegmentedControl(
            title = stringResource(R.string.refresh_frequency),
            segmentedControl = {
                val items = RefreshFrequency.entries.toTypedArray()

                val itemTitles = listOf(
                    stringResource(R.string._1_min),
                    stringResource(R.string._5_mins),
                    stringResource(R.string.auto)
                )

                SegmentedControl(
                    items = itemTitles,
                    defaultSelectedItemIndex = items.indexOfFirst { it.value == refreshFrequency.value.value },
                    color = colorScheme.primary
                ) {
                    refreshFrequency.value = items[it]
                    config.refreshFrequency = items[it]
                }
            }, footer = buildAnnotatedString {
                append(stringResource(R.string.foxess_cloud_data_is_updated_every_5_minutes_auto_attempts_to_synchronise_fetches_just_after_the_data_feed_uploads_to_minimise_server_load))
            })
    }
}

@Preview(
    showBackground = true
)
@Composable
fun RefreshFrequencySettingsViewPreview() {
    EnergyStatsTheme {
        RefreshFrequencySettingsView(FakeConfigManager())
    }
}