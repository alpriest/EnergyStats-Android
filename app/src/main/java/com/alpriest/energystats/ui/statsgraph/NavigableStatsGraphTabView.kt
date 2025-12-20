package com.alpriest.energystats.ui.statsgraph

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.tabs.TopBarSettings
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

enum class StatsScreen {
    Graph,
    CustomDateRangeEditor
}

class NavigableStatsGraphTabView(
    private val topBarSettings: MutableState<TopBarSettings>,
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val onWriteTempFile: (String, String) -> Uri?,
    private val filePathChooser: (filename: String, action: (Uri) -> Unit) -> Unit?,
    private val themeStream: MutableStateFlow<AppTheme>,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(viewModel: NavigableStatsGraphTabViewModel = viewModel(factory = NavigableStatsGraphTabViewModelFactory())) {
        trackScreenView("Stats Tab", "NavigableStatsGraphTabView")
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = StatsScreen.Graph.name
        ) {
            composable(StatsScreen.Graph.name) {
                StatsTabView(
                    viewModel.displayModeStream,
                    topBarSettings,
                    configManager,
                    network,
                    onWriteTempFile,
                    filePathChooser,
                    themeStream,
                    userManager,
                    navController
                ).Content()
            }

            composable(StatsScreen.CustomDateRangeEditor.name) {
                val displayMode = viewModel.displayModeStream.collectAsStateWithLifecycle().value
                val start: LocalDate
                val end: LocalDate
                val unit: CustomDateRangeDisplayUnit

                when (displayMode) {
                    is StatsDisplayMode.Custom -> {
                        start = displayMode.start
                        end = displayMode.end
                        unit = displayMode.unit
                    }
                    else -> {
                        start = LocalDate.now().minusMonths(1).startOfMonth()
                        end = LocalDate.now().endOfMonth()
                        unit = CustomDateRangeDisplayUnit.MONTHS
                    }
                }

                CustomDateRangePickerView(
                    start,
                    end,
                    unit,
                    { navController.popBackStack() },
                    { start, end, unit ->
                        viewModel.updateCustomDateRange(start, end, unit)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}