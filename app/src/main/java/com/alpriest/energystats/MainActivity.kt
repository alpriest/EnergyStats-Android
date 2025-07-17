package com.alpriest.energystats

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.alpriest.energystats.ui.flow.home.dateFormat
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

        setContent {
            PreHomeView(appContainer = appContainer, viewModel = preHomeViewModel)
        }
    }
}

fun parseToLocalDate(input: String): LocalDateTime {
    val simpleDate = SimpleDateFormat(dateFormat, Locale.getDefault()).parse(input)
    if (simpleDate != null) {
        return simpleDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    } else {
        return LocalDateTime.now()
    }
}