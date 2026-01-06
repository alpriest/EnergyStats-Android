package com.alpriest.energystats.tabs

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.shared.models.AppTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ConfigureStatusBarColours(page: Int, themeStream: StateFlow<AppTheme>) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    val isDarkMode = isDarkMode(themeStream)

    DisposableEffect(page) {
        val statusBarDarkIcons = when (page) {
            0 -> !isDarkMode
            else -> false
        }

        val statusBarScrim = Color.Companion.Transparent

        activity.enableEdgeToEdge(
            statusBarStyle =
                if (statusBarDarkIcons)
                    SystemBarStyle.Companion.light(statusBarScrim.toArgb(), statusBarScrim.toArgb()) // dark icons
                else
                    SystemBarStyle.Companion.dark(statusBarScrim.toArgb()), // light icons
            navigationBarStyle = SystemBarStyle.Companion.light(statusBarScrim.toArgb(), statusBarScrim.toArgb())
        )
        onDispose { }
    }
}