package com.alpriest.energystats

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.alpriest.energystats.ui.flow.home.networkDateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val appContainer = (application as EnergyStatsApplication).appContainer
        appContainer.filePathChooser = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) {
            it?.let {
                appContainer.filePathChooserCallback?.invoke(it)
            }
        }
        val preHomeViewModel = PreHomeViewModel(
            appContainer.networking,
            appContainer.configManager,
            appContainer.solarForecastingProvider
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PreHomeView(appContainer = appContainer, viewModel = preHomeViewModel)
        }
    }
}

fun parseToLocalDateTime(input: String): LocalDateTime {
    val simpleDate = SimpleDateFormat(networkDateFormat, Locale.getDefault()).parse(input)
    return if (simpleDate != null) {
        simpleDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    } else {
        LocalDateTime.now()
    }
}

@Composable
fun SystemBars(
    statusBarDarkIcons: Boolean,
    navigationBarDarkIcons: Boolean = false,
    statusBarScrim: Color = Color.Transparent,
    navigationBarScrim: Color = Color.Transparent
) {
    val activity = LocalContext.current as ComponentActivity
    SideEffect {
        activity.enableEdgeToEdge(
            statusBarStyle =
                if (statusBarDarkIcons)
                    SystemBarStyle.light(statusBarScrim.toArgb(), statusBarScrim.toArgb()) // dark icons
                else
                    SystemBarStyle.dark(statusBarScrim.toArgb()), // light icons
            navigationBarStyle =
                if (navigationBarDarkIcons)
                    SystemBarStyle.light(navigationBarScrim.toArgb(), navigationBarScrim.toArgb())
                else
                    SystemBarStyle.dark(navigationBarScrim.toArgb())
        )
    }
}