package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.inverter.WorkMode
import com.alpriest.energystats.ui.settings.inverter.WorkModeViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class ScheduleSummaryViewModelFactory(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging,
    private val navController: NavController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScheduleSummaryViewModel(network, configManager, navController) as T
    }
}

class ScheduleSummaryViewModel(
    val network: FoxESSNetworking,
    val config: ConfigManaging,
    val navController: NavController
) : ViewModel(), AlertDialogMessageProviding {
    var scheduleStream = MutableStateFlow<Schedule?>(null)
    var uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    override val alertDialogMessage = MutableStateFlow<String?>(null)

}

class ScheduleSummaryView(private val configManager: ConfigManaging, private val network: FoxESSNetworking, private val navController: NavHostController, userManager: UserManaging) {
    @Composable
    fun Content(viewModel: ScheduleSummaryViewModel = viewModel(factory = ScheduleSummaryViewModelFactory(network, configManager, navController))) {
    }
}
