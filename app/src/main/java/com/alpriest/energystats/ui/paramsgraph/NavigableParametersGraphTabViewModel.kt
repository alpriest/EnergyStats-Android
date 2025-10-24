package com.alpriest.energystats.ui.paramsgraph

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.models.solcastPrediction
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.tabs.TopBarSettings
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGraphVariableChooserView
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGraphVariableChooserViewModel
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterVariableGroupEditorView
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterVariableGroupEditorViewModel
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NavigableParametersGraphTabViewModel(val configManager: ConfigManaging) : ViewModel() {
    val graphVariablesStream = MutableStateFlow<List<ParameterGraphVariable>>(listOf())

    init {
        viewModelScope.launch {
            configManager.currentDevice
                .collect { it ->
                    it?.let { _ ->
                        var variables = configManager.variables.mapNotNull { variable: Variable ->
                            val configVariable = configManager.variables.firstOrNull { it.variable == variable.variable }

                            if (configVariable != null) {
                                return@mapNotNull ParameterGraphVariable(
                                    configVariable,
                                    isSelected = selectedGraphVariables().contains(configVariable.variable),
                                    enabled = selectedGraphVariables().contains(configVariable.variable),
                                )
                            } else {
                                return@mapNotNull null
                            }
                        }

                        variables = variables.plus(
                            ParameterGraphVariable(
                                type = Variable.solcastPrediction,
                                isSelected = selectedGraphVariables().contains(Variable.solcastPrediction.variable),
                                enabled = true
                            )
                        )

                        graphVariablesStream.value = variables
                    }
                }
        }
    }

    private fun selectedGraphVariables(): List<String> {
        return configManager.selectedParameterGraphVariables.ifEmpty {
            ParameterGraphVariableChooserViewModel.DefaultGraphVariables
        }
    }
}

class NavigableParametersGraphTabViewModelFactory(
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NavigableParametersGraphTabViewModel(configManager) as T
    }
}

class NavigableParametersGraphTabView(
    val topBarSettings: MutableState<TopBarSettings>,
    val configManager: ConfigManaging,
    val userManager: UserManaging,
    val network: Networking,
    private val onWriteTempFile: (String, String) -> Uri?,
    private val filePathChooser: (filename: String, action: (Uri) -> Unit) -> Unit?,
    val themeStream: MutableStateFlow<AppTheme>,
    private val solarForecastProvider: () -> SolcastCaching
) {
    @Composable
    fun Content(viewModel: NavigableParametersGraphTabViewModel = viewModel(factory = NavigableParametersGraphTabViewModelFactory(configManager))) {
        trackScreenView("Parameters Tab", "NavigableParametersGraphTabView")
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = ParametersScreen.Graph.name
        ) {
            composable(ParametersScreen.Graph.name) {
                ParametersGraphTabView(
                    topBarSettings,
                    network,
                    configManager,
                    userManager,
                    onWriteTempFile,
                    viewModel.graphVariablesStream,
                    navController,
                    filePathChooser,
                    solarForecastProvider
                ).Content(themeStream = themeStream)
            }

            composable(ParametersScreen.ParameterChooser.name) {
                topBarSettings.value = TopBarSettings(true, "Choose Parameters", {}, { navController.popBackStack() })

                ParameterGraphVariableChooserView(
                    configManager,
                    viewModel.graphVariablesStream,
                    navController
                ).Content()
            }

            composable(ParametersScreen.ParameterGroupEditor.name) {
                topBarSettings.value = TopBarSettings(true, "Edit Parameter Group", {}, { navController.popBackStack() })

                ParameterVariableGroupEditorView(
                    ParameterVariableGroupEditorViewModel(configManager, viewModel.graphVariablesStream),
                    navController = navController
                )
            }
        }
    }
}