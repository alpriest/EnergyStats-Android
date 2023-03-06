package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

enum class RefreshFrequency(val value: Int) {
    OneMinute(1),
    FiveMinutes(5),
    Auto(0);

    fun title(): String {
        return when (this) {
            OneMinute -> "1 min"
            FiveMinutes -> "5 mins"
            Auto -> "Auto"
        }
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

@Composable
fun RefreshFrequencySettingsView(config: ConfigManaging) {
    val refreshFrequency = rememberSaveable { mutableStateOf(RefreshFrequency.Auto) }

    Column {
        SettingsTitleView("Refresh frequency")

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
                    it.title(),
                    color = Color.DarkGray
                )
            }
        }

        Text(
            modifier = Modifier,
            text = "FoxESS Cloud data is updated every 5 minutes. Auto attempts to synchronise fetches just after the data feed uploads to minimise server load.",
            color = Color.DarkGray
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