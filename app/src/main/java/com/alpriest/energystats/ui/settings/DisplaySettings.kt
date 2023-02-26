package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun DisplaySettings(config: ConfigManaging, modifier: Modifier = Modifier) {
    val largeDisplayState = rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier
    ) {
        SettingsTitleView("Display")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Increase sizes for large display")
            Checkbox(
                checked = largeDisplayState.value,
                onCheckedChange = {
                    largeDisplayState.value = it
                    config.useLargeDisplay = it
                }
            )
        }
    }
}

@Preview(
    showBackground = true,
    heightDp = 640
)
@Composable
fun DisplaySettingsViewPreview() {
    EnergyStatsTheme {
        DisplaySettings(config = FakeConfigManager(), modifier = Modifier.padding(horizontal = 12.dp))
    }
}