package com.alpriest.energystats.ui.flow

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoAPI
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.flow.home.GenerationViewModel
import com.alpriest.energystats.ui.flow.home.HomePowerFlowViewModel
import com.alpriest.energystats.ui.flow.home.LoadedPowerFlowView
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny
import com.alpriest.energystats.ui.theme.preview
import kotlinx.coroutines.flow.MutableStateFlow

class PowerFlowTabViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val themeStream: MutableStateFlow<AppTheme>,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PowerFlowTabViewModel(network, configManager, themeStream, context) as T
    }
}

fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

class PowerFlowTabView(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val userManager: UserManaging,
    private val themeStream: MutableStateFlow<AppTheme>
) {
    private fun largeRadialGradient(colors: List<Color>) = object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            val biggerDimension = maxOf(size.height, size.width)
            return RadialGradientShader(
                colors = colors,
                center = Offset(x = 150f, y = -100f),
                radius = biggerDimension / 2f,
                colorStops = listOf(0f, 0.95f)
            )
        }
    }

    @Composable
    fun Content(
        viewModel: PowerFlowTabViewModel = viewModel(
            factory = PowerFlowTabViewModelFactory(network, configManager, this.themeStream, LocalContext.current)
        ),
        themeStream: MutableStateFlow<AppTheme>
    ) {
        val loadingBackground = remember { largeRadialGradient(listOf(Color.White, Color.Transparent)) }
        val loadedBackground = remember { largeRadialGradient(listOf(Sunny.copy(alpha = 0.7f), Color.Transparent)) }
        val errorBackground = remember { largeRadialGradient(listOf(Color.Red.copy(alpha = 0.7f), Color.Transparent)) }

        val uiState by viewModel.uiState.collectAsState()
        val showSunnyBackground = themeStream.collectAsState().value.showSunnyBackground
        val background = when (uiState.state) {
            is PowerFlowLoadState.Active -> loadingBackground
            is PowerFlowLoadState.Loaded -> loadedBackground
            is PowerFlowLoadState.Error -> errorBackground
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .conditional(showSunnyBackground) {
                    background(background)
                },
            contentAlignment = TopEnd
        ) {
            when (uiState.state) {
                is PowerFlowLoadState.Active -> LoadingView(stringResource(R.string.loading))
                is PowerFlowLoadState.Loaded -> LoadedView(viewModel, configManager, (uiState.state as PowerFlowLoadState.Loaded).viewModel, themeStream)
                is PowerFlowLoadState.Error -> ErrorView(
                    (uiState.state as PowerFlowLoadState.Error).ex,
                    (uiState.state as PowerFlowLoadState.Error).reason,
                    onRetry = { viewModel.timerFired() },
                    onLogout = { userManager.logout() }
                )
            }
        }
    }
}

@Composable
fun LoadedView(
    viewModel: PowerFlowTabViewModel,
    configManager: ConfigManaging,
    homePowerFlowViewModel: HomePowerFlowViewModel,
    themeStream: MutableStateFlow<AppTheme>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        LoadedPowerFlowView(
            configManager = configManager,
            powerFlowViewModel = viewModel,
            homePowerFlowViewModel = homePowerFlowViewModel,
            themeStream = themeStream
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
fun PowerFlowTabViewPreview() {
    val viewModel = PowerFlowTabViewModel(DemoNetworking(), FakeConfigManager(), MutableStateFlow(AppTheme.preview()), LocalContext.current)

    val homePowerFlowViewModel = HomePowerFlowViewModel(
        solar = 1.0,
        solarStrings = listOf(
            StringPower("pv1", 0.3),
            StringPower("pv2", 0.7)
        ),
        home = 2.45,
        grid = 2.45,
        todaysGeneration = GenerationViewModel(response = OpenHistoryResponse(deviceSN = "1", datas = listOf()), false),
        earnings = EarningsViewModel.preview(),
        inverterTemperatures = null,
        hasBattery = true,
        battery = BatteryViewModel(),
        FakeConfigManager(),
        homeTotal = 1.0,
        gridImportTotal = 1.0,
        gridExportTotal = 2.0,
        ct2 = 0.4,
    )

    EnergyStatsTheme {
        LoadedView(
            viewModel = viewModel,
            configManager = FakeConfigManager(),
            homePowerFlowViewModel = homePowerFlowViewModel,
            themeStream = MutableStateFlow(AppTheme.preview())
        )
    }
}

data class UiPowerFlowLoadState(
    val state: PowerFlowLoadState
)

sealed class PowerFlowLoadState {
    data class Error(val ex: Exception, val reason: String) : PowerFlowLoadState()
    data class Active(val value: String) : PowerFlowLoadState()
    data class Loaded(val viewModel: HomePowerFlowViewModel) : PowerFlowLoadState()
}

data class UiLoadState(
    val state: LoadState
)

sealed class LoadState {
    object Inactive : LoadState()
    data class Error(val ex: Exception?, val reason: String) : LoadState()
    data class Active(val value: String) : LoadState()
}
