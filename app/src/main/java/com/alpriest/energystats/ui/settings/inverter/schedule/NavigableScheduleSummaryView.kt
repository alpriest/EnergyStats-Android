package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.ScheduleTemplateListView

enum class ScheduleScreen {
    Summary,
    EditSchedule,
    EditPhase,
    TemplateList;
}

class NavigableScheduleSummaryView(
    private val configManager: ConfigManaging,
    private val network: FoxESSNetworking,
    private val userManager: UserManaging
) {
    @Composable
    fun Content() {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = ScheduleScreen.Summary.name
        ) {
            composable(ScheduleScreen.Summary.name) {
                ScheduleSummaryView(configManager, network, navController, userManager).Content()
            }

            composable(ScheduleScreen.EditSchedule.name) {
                EditScheduleView(
                    configManager,
                    network,
                    navController,
                    userManager
                ).Content()
            }

            composable(ScheduleScreen.EditPhase.name) {
                EditPhaseView(navController)
            }

            composable(ScheduleScreen.TemplateList.name) {
                ScheduleTemplateListView()
            }
        }
    }
}
