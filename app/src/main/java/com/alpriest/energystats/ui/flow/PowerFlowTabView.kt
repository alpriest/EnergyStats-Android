package com.alpriest.energystats.ui.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.flow.home.LoadedPowerFlowView
import com.alpriest.energystats.ui.flow.home.HomePowerFlowViewModel
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PowerFlowTabViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val themeStream: MutableStateFlow<AppTheme>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Networking::class.java, ConfigManaging::class.java, MutableStateFlow::class.java)
            .newInstance(network, configManager, themeStream)
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
            factory = PowerFlowTabViewModelFactory(network, configManager, this.themeStream)
        ),
        themeStream: MutableStateFlow<AppTheme>
    ) {
        val loadingBackground = remember { largeRadialGradient(listOf(Color.White, Color.Transparent)) }
        val loadedBackground = remember { largeRadialGradient(listOf(Sunny, Color.Transparent)) }
        val errorBackground = remember { largeRadialGradient(listOf(Color.Red.copy(alpha = 0.7f), Color.Transparent)) }

        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val showSunnyBackground = themeStream.collectAsState().value.showSunnyBackground
        val background = when (uiState.state) {
            is LoadingLoadState -> loadingBackground
            is LoadedLoadState -> loadedBackground
            is ErrorLoadState -> errorBackground
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
                is LoadingLoadState -> LoadingView(stringResource(R.string.loading))
                is ErrorLoadState -> Error((uiState.state as ErrorLoadState).reason) { coroutineScope.launch { viewModel.timerFired() } }
                is LoadedLoadState -> Loaded(viewModel, (uiState.state as LoadedLoadState).viewModel, themeStream)
            }
        }
    }

    @Composable
    fun Error(reason: String, onRetry: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Rounded.ErrorOutline,
                tint = Color.Red,
                contentDescription = "",
                modifier = Modifier.size(128.dp)
            )
            Text(
                reason,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(stringResource(R.string.retry))
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    @Composable
    fun Loaded(
        viewModel: PowerFlowTabViewModel,
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
}

@Preview(showBackground = true, heightDp = 700)
@Composable
fun PowerFlowTabViewPreview() {
    val viewModel = PowerFlowTabViewModel(DemoNetworking(), FakeConfigManager(), MutableStateFlow(AppTheme.preview()))

    val homePowerFlowViewModel = HomePowerFlowViewModel(
        solar = 1.0,
        home = 2.45,
        grid = 2.45,
        todaysGeneration = 5.4,
        earnings = "Earnings £2.52 · £12.28 · £89.99 · £145.99",
        inverterTemperatures = null,
        hasBattery = true,
        battery = BatteryViewModel(),
        FakeConfigManager(),
        homeTotal = 1.0,
        gridImportTotal = 1.0,
        gridExportTotal = 2.0
    )

    EnergyStatsTheme {
        PowerFlowTabView(
            DemoNetworking(),
            FakeConfigManager(),
            MutableStateFlow(AppTheme.preview())
        ).Loaded(
            viewModel = viewModel,
            homePowerFlowViewModel = homePowerFlowViewModel,
            themeStream = MutableStateFlow(AppTheme.preview())
        )
    }
}

data class UiLoadState(
    val state: LoadState
)

sealed class LoadState
class ErrorLoadState(val reason: String) : LoadState()
object LoadingLoadState : LoadState()
class LoadedLoadState(val viewModel: HomePowerFlowViewModel) : LoadState()
