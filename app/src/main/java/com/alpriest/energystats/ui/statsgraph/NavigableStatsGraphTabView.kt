package com.alpriest.energystats.ui.statsgraph

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.tabs.TopBarSettings
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.statsgraph.StatsDisplayMode.Day
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
    val displayModeStream = MutableStateFlow<StatsDisplayMode>(Day(LocalDate.now()))

    @Composable
    fun Content() {
        trackScreenView("Stats Tab", "NavigableStatsGraphTabView")
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = StatsScreen.Graph.name
        ) {
            composable(StatsScreen.Graph.name) {
                StatsTabView(
                    displayModeStream,
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
                CustomDateRangePickerView(
                    LocalDate.now(),
                    LocalDate.now(),
                    CustomDateRangeDisplayUnit.MONTHS,
                    { navController.popBackStack() },
                    { start, end ->
                        updateCustomDateRange(start, end)
                        navController.popBackStack()
                    }
                )
//                val scope = rememberCoroutineScope()
//                val start = viewModel.customStartDate.collectAsStateWithLifecycle().value
//                val end = viewModel.customEndDate.collectAsStateWithLifecycle().value

//                CustomDateRangePickerView(
//                    start,
//                    end,
//                    CustomDateRangeDisplayUnit.MONTHS,
//                    {
//                        scope.launch { sheetState.hide() }.invokeOnCompletion {
//                            if (!sheetState.isVisible) {
//                                onShowingCustomDateRangePickerChange(false)
//                            }
//                        }
//                    },
//                    { start, end ->
//                        viewModel.updateCustomDateRange(start, end)
//                        scope.launch { sheetState.hide() }.invokeOnCompletion {
//                            if (!sheetState.isVisible) {
//                                onShowingCustomDateRangePickerChange(false)
//                            }
//                        }
//                    }
//                )
            }
        }
    }

    fun updateCustomDateRange(start: LocalDate, end: LocalDate) {
        if (start > end) {
            return
        }

        val daysBetween = ChronoUnit.DAYS.between(start, end)
        val displayUnit = if (daysBetween > 31) CustomDateRangeDisplayUnit.MONTHS else CustomDateRangeDisplayUnit.DAYS

        val normalizedStart: LocalDate
        val normalizedEnd: LocalDate

        if (displayUnit == CustomDateRangeDisplayUnit.MONTHS) {
            normalizedStart = start.withDayOfMonth(1)
            normalizedEnd = end.withDayOfMonth(end.lengthOfMonth())
        } else {
            normalizedStart = start
            normalizedEnd = end
        }

        displayModeStream.value = StatsDisplayMode.Custom(normalizedStart, normalizedEnd, displayUnit)
    }
}