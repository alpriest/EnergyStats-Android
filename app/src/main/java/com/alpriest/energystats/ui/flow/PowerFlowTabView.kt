package com.alpriest.energystats.ui.flow

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import com.alpriest.energystats.ui.login.UserManager
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

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
        val loadedBackground = remember { largeRadialGradient(listOf(Sunny, Color.Transparent)) }
        val errorBackground = remember { largeRadialGradient(listOf(Color.Red.copy(alpha = 0.7f), Color.Transparent)) }

        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
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
                    (uiState.state as PowerFlowLoadState.Error).reason,
                    onRetry = { coroutineScope.launch { viewModel.timerFired() } },
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

@Preview
@Composable
fun ErrorPreview() {
    ErrorView(reason = "BEGIN_OBJECT was expected but got something else instead", onRetry = {}, onLogout = {})
}

@Composable
fun ErrorView(reason: String, onRetry: suspend () -> Unit, onLogout: () -> Unit) {
    var showingDetail by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        Icon(
            Icons.Rounded.ErrorOutline,
            tint = Color.Red,
            contentDescription = "",
            modifier = Modifier
                .size(128.dp)
                .clickable { showingDetail = !showingDetail }
        )
        Text(
            text = stringResource(R.string.something_went_wrong_fetching_data_from_foxess_cloud),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp),
            color = colors.onPrimary
        )
        Text(
            stringResource(R.string.tap_the_icon_for_further_detail),
            textAlign = TextAlign.Center,
            color = colors.onPrimary
        )

        Button(
            onClick = { coroutineScope.launch { onRetry() } },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(
                stringResource(R.string.retry),
                color = colors.onSecondary,
            )
        }

        Button(
            onClick = { uriHandler.openUri("https://monitor.foxesscommunity.com/status/foxess") },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(end = 12.dp)
        ) {
            Text(
                stringResource(R.string.foxess_cloud_status),
                color = colors.onSecondary,
            )
        }

        Button(
            onClick = { onLogout() },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(end = 12.dp)
        ) {
            Text(
                stringResource(R.string.logout),
                color = colors.onSecondary,
            )
        }

        if (showingDetail) {
            Dialog(
                onDismissRequest = { showingDetail = false },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.background)
                        .padding(12.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        reason,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
fun PowerFlowTabViewPreview() {
    val viewModel = PowerFlowTabViewModel(DemoNetworking(), FakeConfigManager(), MutableStateFlow(AppTheme.preview()), LocalContext.current)

    val homePowerFlowViewModel = HomePowerFlowViewModel(
        solar = 1.0,
        home = 2.45,
        grid = 2.45,
        todaysGeneration = 5.4,
        earnings = EarningsViewModel.preview(),
        inverterTemperatures = null,
        hasBattery = true,
        battery = BatteryViewModel(),
        FakeConfigManager(),
        homeTotal = 1.0,
        gridImportTotal = 1.0,
        gridExportTotal = 2.0
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
    data class Error(val reason: String) : PowerFlowLoadState()
    data class Active(val value: String) : PowerFlowLoadState()
    data class Loaded(val viewModel: HomePowerFlowViewModel) : PowerFlowLoadState()
}

data class UiLoadState(
    val state: LoadState
)

sealed class LoadState {
    object Inactive : LoadState()
    data class Error(val reason: String) : LoadState()
    data class Active(val value: String) : LoadState()
}
