package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.login.UserManaging

enum class ScheduleScreen {
    Summary,
    EditSchedule,
    EditPhase
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
            startDestination = ScheduleScreen.Summary.name,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
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
        }
    }
}
