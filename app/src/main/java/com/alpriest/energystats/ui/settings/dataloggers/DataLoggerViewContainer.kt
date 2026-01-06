package com.alpriest.energystats.ui.settings.dataloggers

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.models.network.DataLoggerStatus
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.inverter.SettingsRow
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

data class DataLogger(
    val moduleSN: String,
    val stationID: String,
    val signal: Int,
    val status: DataLoggerStatus
)

class DataLoggerViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DataLoggerViewModel(network, configManager, navController) as T
    }
}

class DataLoggerViewModel(
    val network: Networking,
    val config: ConfigManaging,
    val navController: NavController
) : ViewModel() {
    var itemStream: MutableStateFlow<List<DataLogger>> = MutableStateFlow(listOf())
    var uiState = MutableStateFlow<UiLoadState>(UiLoadState(LoadState.Inactive))

    suspend fun load(context: Context) {
        uiState.value = UiLoadState(LoadState.Active.Loading)

        runCatching {
            try {
                val result = network.fetchDataLoggers()
                itemStream.value = result.map {
                    DataLogger(
                        moduleSN = it.moduleSN,
                        stationID = it.stationID,
                        signal = it.signal,
                        status = it.status
                    )
                }
                uiState.value = UiLoadState(LoadState.Inactive)
            } catch (ex: Exception) {
                uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: context.getString(R.string.unknown_error), true))
            }
        }
    }
}

class DataLoggerViewContainer(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController
) {
    @Composable
    fun Content(viewModel: DataLoggerViewModel = viewModel(factory = DataLoggerViewModelFactory(network, configManager, navController)), modifier: Modifier) {
        val loadState = viewModel.uiState.collectAsState().value.state
        val context = LocalContext.current

        LaunchedEffect(null) {
            viewModel.load(context)
        }
        trackScreenView("Datalogger", "DataLoggerViewContainer")

        val items = viewModel.itemStream.collectAsState()

        when (loadState) {
            is LoadState.Active ->
                LoadingView(loadState)
            is LoadState.Error ->
                ErrorView(loadState.ex, loadState.reason, onRetry = { viewModel.load(context) }, onLogout = { }, allowRetry = true)
            is LoadState.Inactive ->
                SettingsPage(modifier) {
                    items.value.map {
                        DataLoggerView(it)
                    }
                }
        }
    }
}

@Composable
fun DataLoggerView(dataLogger: DataLogger) {
    SettingsColumnWithChild {
        SettingsRow("Module SN", dataLogger.moduleSN)
        SettingsRow("Station ID", dataLogger.stationID)
        SettingsRow("Signal") { SignalStrengthView(dataLogger.signal) }
        SettingsRow("Status") {
            if (dataLogger.status == DataLoggerStatus.ONLINE) {
                Icon(imageVector = Icons.Default.CheckCircle, tint = Color.Green, contentDescription = "Connected")
            } else {
                Icon(imageVector = Icons.Default.Cancel, tint = Color.Red, contentDescription = "Disconnected")
            }
        }
    }
}

@Composable
fun SignalStrengthView(amount: Int) {
    Row(
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(5, 10, 15, 20).forEachIndexed { index, height ->
            Rectangle(
                color = if (index < amount) colorScheme.onSurface else colorScheme.surfaceVariant,
                modifier = Modifier
                    .width(4.dp)
                    .height(height.dp)
            )

            Spacer(modifier = Modifier.padding(horizontal = 1.dp))
        }
    }
}

@Composable
fun Rectangle(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RectangleShape)
            .background(color)
    )
}

@Preview(showBackground = true)
@Composable
fun DataloggerViewPreview() {
    EnergyStatsTheme {
        DataLoggerView(
            dataLogger = DataLogger(moduleSN = "ABC123DEF456", stationID = "W2", signal = 2, status = DataLoggerStatus.ONLINE),
        )
    }
}
