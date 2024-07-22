package com.alpriest.energystats.ui.settings.dataloggers

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
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
import com.alpriest.energystats.models.DataLoggerStatus
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
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
) : ViewModel(), AlertDialogMessageProviding {
    var itemStream: MutableStateFlow<List<DataLogger>> = MutableStateFlow(listOf())
    var activityStream = MutableStateFlow<String?>(null)
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    suspend fun load(context: Context) {
        activityStream.value = context.getString(R.string.loading)

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
            } catch (ex: Exception) {
                alertDialogMessage.value = MonitorAlertDialogData(ex, ex.localizedMessage)
            }
        }.also {
            activityStream.value = null
        }
    }
}

class DataLoggerViewContainer(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController
) {
    @Composable
    fun Content(viewModel: DataLoggerViewModel = viewModel(factory = DataLoggerViewModelFactory(network, configManager, navController))) {
        val isActive = viewModel.activityStream.collectAsState().value
        val context = LocalContext.current

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        val items = viewModel.itemStream.collectAsState()

        isActive?.let {
            LoadingView(it)
        } ?: run {
            Column(modifier = Modifier.padding(12.dp)) {
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
                color = if (index < amount) colors.onSurface else colors.primaryVariant,
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
