package com.alpriest.energystats.ui.flow

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.flow.home.LoadedPowerFlowView
import com.alpriest.energystats.ui.flow.home.LoadedPowerFlowViewModel
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny
import com.alpriest.energystats.ui.theme.demo
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
        var showSlowServerBanner by remember { mutableStateOf(true) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .conditional(showSunnyBackground) {
                    background(background)
                },
            contentAlignment = TopEnd
        ) {
            Column {
                SlowServerBannerView { showSlowServerBanner = !showSlowServerBanner }

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

            AnimatedVisibility(
                visible = showSlowServerBanner,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                SlowServerMessageView { showSlowServerBanner = false }
            }
        }
    }

    @Composable
    private fun SlowServerMessageView(dismiss: () -> Unit) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
                .shadow(elevation = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.slow_performance),
                    color = MaterialTheme.colors.onSecondary,
                    style = MaterialTheme.typography.h2
                )

                Text(
                    stringResource(R.string.slow_performance_message),
                    color = MaterialTheme.colors.onSecondary
                )

                Image(
                    painter = painterResource(R.drawable.slow_performance),
                    contentDescription = "Slow performance",
                )

                Button(onClick = { dismiss() }) {
                    Text(stringResource(id = R.string.ok))
                }
            }
        }
    }

    @Composable
    private fun SlowServerBannerView(onToggle: () -> Unit) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red)
                .clickable { onToggle() }
        ) {
            Text(
                "Always loading? Tap for details",
                color = Color.White
            )
        }
    }
}

@Composable
fun LoadedView(
    viewModel: PowerFlowTabViewModel,
    configManager: ConfigManaging,
    loadedPowerFlowViewModel: LoadedPowerFlowViewModel,
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
            loadedPowerFlowViewModel = loadedPowerFlowViewModel,
            themeStream = themeStream
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
fun PowerFlowTabViewPreview() {
    val viewModel = PowerFlowTabViewModel(DemoNetworking(), FakeConfigManager(), MutableStateFlow(AppTheme.demo()), LocalContext.current)
    val themeStream = MutableStateFlow(AppTheme.demo())

    EnergyStatsTheme {
        PowerFlowTabView(DemoNetworking(), FakeConfigManager(), FakeUserManager(), themeStream).Content(
            viewModel = viewModel,
            themeStream = themeStream
        )
    }
}

fun Device.Companion.preview(): Device {
    return Device("", true, "", "", true, "", null, "")
}

data class UiPowerFlowLoadState(
    val state: PowerFlowLoadState
)

sealed class PowerFlowLoadState {
    data class Error(val ex: Exception, val reason: String) : PowerFlowLoadState()
    data class Active(val value: String) : PowerFlowLoadState()
    data class Loaded(val viewModel: LoadedPowerFlowViewModel) : PowerFlowLoadState()
}

data class UiLoadState(
    val state: LoadState
)

sealed class LoadState {
    object Inactive : LoadState()
    data class Error(val ex: Exception?, val reason: String) : LoadState()
    data class Active(val value: String) : LoadState()

    companion object
}
