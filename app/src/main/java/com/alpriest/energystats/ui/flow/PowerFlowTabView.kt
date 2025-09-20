@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.alpriest.energystats.ui.flow

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.tabs.TopBarSettings
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeConfigStore
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.WidgetDataSharer
import com.alpriest.energystats.stores.WidgetDataSharing
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.flow.home.LoadedPowerFlowView
import com.alpriest.energystats.ui.flow.home.LoadedPowerFlowViewModel
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleSummaryView
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStore
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStoring
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

class PowerFlowTabViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val themeStream: MutableStateFlow<AppTheme>,
    private val context: Context,
    private val widgetDataSharer: WidgetDataSharing,
    private val bannerAlertManager: BannerAlertManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PowerFlowTabViewModel(network, configManager, themeStream, context, widgetDataSharer, bannerAlertManager) as T
    }
}

@Composable
fun Modifier.conditional(condition: Boolean, modifier: @Composable Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

class PowerFlowTabView(
    private val topBarSettings: MutableState<TopBarSettings>,
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val userManager: UserManaging,
    private val themeStream: MutableStateFlow<AppTheme>,
    private val widgetDataSharer: WidgetDataSharing,
    private val bannerAlertManager: BannerAlertManaging,
    private val templateStore: TemplateStoring
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
            factory = PowerFlowTabViewModelFactory(network, configManager, this.themeStream, LocalContext.current, widgetDataSharer, bannerAlertManager)
        ),
        themeStream: MutableStateFlow<AppTheme>
    ) {
        trackScreenView("Power Flow Tab", "PowerFlowTabView")
        val loadingBackground = remember { largeRadialGradient(listOf(Color.White, Color.Transparent)) }
        val loadedBackground = remember { largeRadialGradient(listOf(Sunny.copy(alpha = 0.7f), Color.Transparent)) }
        val errorBackground = remember { largeRadialGradient(listOf(Color.Red.copy(alpha = 0.7f), Color.Transparent)) }
        topBarSettings.value = TopBarSettings(false, "", {}, null)

        val uiState = viewModel.uiState.collectAsStateWithLifecycle().value.state
        val showSunnyBackground = themeStream.collectAsStateWithLifecycle().value.showSunnyBackground
        val background = when (uiState) {
            is PowerFlowLoadState.Active -> loadingBackground
            is PowerFlowLoadState.Loaded -> loadedBackground
            is PowerFlowLoadState.Error -> errorBackground
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .conditional(showSunnyBackground) {
                    background(background)
                }
                .windowInsetsPadding(WindowInsets.statusBars),
            contentAlignment = TopEnd
        ) {
            when (uiState) {
                is PowerFlowLoadState.Active -> LoadingView(stringResource(R.string.loading))
                is PowerFlowLoadState.Loaded -> LoadedView(viewModel, configManager, uiState.viewModel, themeStream, network, userManager, templateStore)
                is PowerFlowLoadState.Error -> ErrorView(
                    uiState.ex,
                    uiState.reason,
                    true,
                    onRetry = { viewModel.timerFired() },
                    onLogout = { userManager.logout() }
                )
            }

            BannerView(viewModel.bannerAlertStream)
        }
    }
}

@Composable
fun LoadedView(
    viewModel: PowerFlowTabViewModel,
    configManager: ConfigManaging,
    loadedPowerFlowViewModel: LoadedPowerFlowViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    network: Networking,
    userManager: UserManaging,
    templateStore: TemplateStoring
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val navController = NavHostController(LocalContext.current)
    val appSettings = themeStream.collectAsState().value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Box {
            if (appSettings.showInverterScheduleQuickLink) {
                Column(
                    modifier = Modifier.align(TopEnd)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Inverter schedule",
                        modifier = Modifier
                            .clickable {
                                showBottomSheet = true
                            }.align(Alignment.End)
                    )

                    appSettings.detectedActiveTemplate?.let {
                        Text(it)
                    }
                }
            }

            LoadedPowerFlowView(
                configManager = configManager,
                powerFlowViewModel = viewModel,
                loadedPowerFlowViewModel = loadedPowerFlowViewModel,
                themeStream = themeStream
            )

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState,
                    contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom) }
                ) {
                    Column(modifier = Modifier.fillMaxHeight()) {
                        ScheduleSummaryView(configManager, network, navController, userManager, templateStore).Content(modifier = Modifier)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
fun PowerFlowTabViewPreview() {
    val powerFlowTabViewModel = PowerFlowTabViewModel(
        DemoNetworking(),
        FakeConfigManager(),
        MutableStateFlow(AppTheme.demo()),
        LocalContext.current,
        WidgetDataSharer(FakeConfigStore()),
        BannerAlertManager()
    )
    val loadedPowerFlowViewModel = LoadedPowerFlowViewModel(
        LocalContext.current,
        currentValuesStream = MutableStateFlow(CurrentValues(2.45, 2.45, null, 0.4, 1.0, listOf(
            StringPower("pv1", 0.3),
            StringPower("pv2", 0.7)
        ))),
        hasBattery = true,
        battery = BatteryViewModel(),
        FakeConfigManager(),
        currentDevice = Device.preview(),
        network = DemoNetworking(),
        BannerAlertManager()
    )
    val themeStream = MutableStateFlow(AppTheme.demo())

    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        LoadedView(
            viewModel = powerFlowTabViewModel,
            FakeConfigManager(),
            loadedPowerFlowViewModel,
            themeStream,
            DemoNetworking(),
            FakeUserManager(),
            TemplateStore(FakeConfigManager())
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
    data object Inactive : LoadState()
    data class Error(val ex: Exception?, val reason: String, val allowRetry: Boolean = true) : LoadState()
    data class Active(val value: String) : LoadState()

    companion object
}
