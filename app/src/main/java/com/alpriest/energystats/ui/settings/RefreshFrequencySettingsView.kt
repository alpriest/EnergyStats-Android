package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            RefreshFrequency.values().map {
                RadioButton(
                    selected = refreshFrequency.value == it,
                    onClick = {
                        refreshFrequency.value = it
                        config.refreshFrequency = it
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
                )
                Text(
                    when (it) {
                        RefreshFrequency.OneMinute -> stringResource(R.string._1_min)
                        RefreshFrequency.FiveMinutes -> stringResource(R.string._5_mins)
                        RefreshFrequency.Auto -> stringResource(R.string.auto)
                    }
                )
            }
        }

        Text(
            modifier = Modifier,
            text = stringResource(R.string.foxess_cloud_data_is_updated_every_5_minutes_auto_attempts_to_synchronise_fetches_just_after_the_data_feed_uploads_to_minimise_server_load),
            color = colors.onSecondary
        )
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