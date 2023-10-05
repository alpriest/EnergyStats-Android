package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alpriest.energystats.stores.ConfigManaging

@Composable
fun ApproximationsSettingsView(config: ConfigManaging) {
    SettingsPage {
        SelfSufficiencySettingsView(config, modifier = Modifier.fillMaxWidth())
        FinancialsSettingsView(config = config)
    }
}
