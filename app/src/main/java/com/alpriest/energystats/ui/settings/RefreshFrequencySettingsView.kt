package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.SegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

enum class RefreshFrequency(val value: Int) {
    OneMinute(1),
    FiveMinutes(5),
    Auto(0);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

@Composable
fun RefreshFrequencySettingsView(config: ConfigManaging) {
    val refreshFrequency = rememberSaveable { mutableStateOf(RefreshFrequency.Auto) }

    SettingsColumnWithChild {
        SettingsTitleView(stringResource(R.string.refresh_frequency))

        SettingsSegmentedControl(title = null, segmentedControl = {
            val items = RefreshFrequency.values()

            val itemTitles = listOf(
                stringResource(R.string._1_min),
                stringResource(R.string._5_mins),
                stringResource(R.string.auto)
            )

            SegmentedControl(
                items = itemTitles,
                defaultSelectedItemIndex = items.indexOf(refreshFrequency.value),
                useFixedWidth = true,
                color = colors.primary
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