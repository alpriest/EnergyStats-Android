package com.alpriest.energystats.ui.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.CardDefaults.outlinedCardColors
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawDataStore
import com.alpriest.energystats.models.RawDataStoring
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.flow.home.SummaryPowerFlowView
import com.alpriest.energystats.ui.flow.home.SummaryPowerFlowViewModel
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PowerFlowTabViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val rawDataStore: RawDataStoring
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Networking::class.java, ConfigManaging::class.java, RawDataStoring::class.java)
            .newInstance(network, configManager, rawDataStore)
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
    private val rawDataStore: RawDataStoring
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
            factory = PowerFlowTabViewModelFactory(network, configManager, rawDataStore)
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
                is LoadingLoadState -> LoadingView("Loading...").run {
                    viewModel.timerFired()
                }
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
                Text("Retry")
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    @Composable
    fun Loaded(
        viewModel: PowerFlowTabViewModel,
        summaryPowerFlowViewModel: SummaryPowerFlowViewModel,
        themeStream: MutableStateFlow<AppTheme>
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(12.dp)
        ) {
            SummaryPowerFlowView(
                powerFlowViewModel = viewModel,
                summaryPowerFlowViewModel = summaryPowerFlowViewModel,
                themeStream = themeStream
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
fun PowerFlowTabViewPreview() {
    val viewModel = PowerFlowTabViewModel(DemoNetworking(), FakeConfigManager(), RawDataStore())
    val now = SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date())
    val homePowerFlowViewModel = SummaryPowerFlowViewModel(
        FakeConfigManager(),
        2.3,
        0.5,
        raw = listOf(
            RawResponse("feedInPower", arrayListOf(RawData(now, 2.45))),
            RawResponse("generationPower", arrayListOf(RawData(now, 2.45))),
            RawResponse("batChargePower", arrayListOf(RawData(now, 2.45))),
            RawResponse("batDischargePower", arrayListOf(RawData(now, 2.45))),
            RawResponse("gridConsumptionPower", arrayListOf(RawData(now, 2.45))),
            RawResponse("loadsPower", arrayListOf(RawData(now, 2.45)))
        ),
        13.6,
        todaysGeneration = 5.4,
    )

    EnergyStatsTheme {
        PowerFlowTabView(
            DemoNetworking(),
            FakeConfigManager(),
            RawDataStore()
        ).Loaded(
            viewModel = viewModel,
            summaryPowerFlowViewModel = homePowerFlowViewModel,
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
class LoadedLoadState(val viewModel: SummaryPowerFlowViewModel) : LoadState()
