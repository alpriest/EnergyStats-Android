package com.alpriest.energystats.ui.settings.dataloggers

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alpriest.energystats.R
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.inverter.InverterWorkMode
import com.alpriest.energystats.ui.settings.inverter.WorkMode
import kotlinx.coroutines.flow.MutableStateFlow

data class Datalogger(
    val moduleSN: String,
    val moduleType: String,
    val plantName: String,
    val version: String,
    val signal: Int,
    val communication: Int
)

class DataloggerViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Networking::class.java, ConfigManaging::class.java, NavController::class.java, Context::class.java)
            .newInstance(network, configManager, navController, context)
    }
}

class DataloggerViewModel(
    val network: Networking,
    val config: ConfigManaging,
    val navController: NavController,
    val context: Context
) : ViewModel() {
    var itemStream: MutableStateFlow<List<Datalogger>> = MutableStateFlow(listOf())
    var activityStream = MutableStateFlow<String?>(null)

    suspend fun load() {
        activityStream.value = context.getString(R.string.loading)

        run {
            try {
                val result = network.fetchDataLoggers()
                itemStream.value = result.data.map {
                    Datalogger(
                        moduleSN = it.moduleSN,
                        moduleType = it.moduleType,
                        plantName = it.plantName,
                        version = it.version,
                        signal = it.signal,
                        communication = it.communication
                    )
                }
            } catch (ex: Exception) {
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}

class DataloggerView(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController,
    private val context: Context
) {
    @Composable
    fun Content(viewModel: DataloggerViewModel = viewModel(factory = DataloggerViewModelFactory(network, configManager, navController, context))) {

    }
}