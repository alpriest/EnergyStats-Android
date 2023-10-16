package com.alpriest.energystats.ui.paramsgraph

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGraphVariableChooserView
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGraphVariableChooserViewModel
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterVariableGroupEditorView
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterVariableGroupEditorViewModel
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NavigableParametersGraphTabViewModel(val configManager: ConfigManaging) : ViewModel() {
    val graphVariablesStream = MutableStateFlow<List<ParameterGraphVariable>>(listOf())

    init {
        viewModelScope.launch {
            configManager.currentDevice
                .collect { it ->
                    it?.let { device ->
                        graphVariablesStream.value = device.variables.mapNotNull { rawVariable: RawVariable ->
                            val variable = configManager.variables.firstOrNull { it.variable == rawVariable.variable }

                            if (variable != null) {
                                return@mapNotNull ParameterGraphVariable(
                                    variable,
                                    isSelected = selectedGraphVariables().contains(variable.variable),
                                    enabled = selectedGraphVariables().contains(variable.variable),
                                )
                            } else {
                                return@mapNotNull null
                            }
                        }
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
    val configManager: ConfigManaging,
    val network: Networking,
    val onWriteTempFile: (String, String) -> Uri?,
    val themeStream: MutableStateFlow<AppTheme>,
) {
    @Composable
    fun Content(viewModel: NavigableParametersGraphTabViewModel = viewModel(factory = NavigableParametersGraphTabViewModelFactory(configManager))) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = ParametersScreen.Graph.name,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(ParametersScreen.Graph.name) {
                ParametersGraphTabView(network, configManager, onWriteTempFile, viewModel.graphVariablesStream, navController).Content(themeStream = themeStream)
            }

            composable(ParametersScreen.ParameterChooser.name) {
                ParameterGraphVariableChooserView(
                    configManager,
                    viewModel.graphVariablesStream,
                    navController
                ).Content(onCancel = { navController.popBackStack() })
            }

            composable(ParametersScreen.ParameterGroupEditor.name) {
                ParameterVariableGroupEditorView(
                    ParameterVariableGroupEditorViewModel(configManager, viewModel.graphVariablesStream),
                    navController = navController
                )
            }
        }
    }
}