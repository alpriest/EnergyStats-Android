package com.alpriest.energystats.ui.settings

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.alpriest.energystats.stores.ConfigManaging

@Composable
fun PowerStationView(config: ConfigManaging, navController: NavHostController) {
    SettingsPage {
        Text("hi")
    }
}