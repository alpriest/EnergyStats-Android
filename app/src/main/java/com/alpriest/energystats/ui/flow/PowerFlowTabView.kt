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
import com.alpriest.energystats.models.RawDataStore
import com.alpriest.energystats.models.RawDataStoring
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.flow.home.SummaryPowerFlowView
import com.alpriest.energystats.ui.flow.home.SummaryPowerFlowViewModel
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

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
        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val background: ShaderBrush = when (uiState.loadState) {
            is Loading -> largeRadialGradient(listOf(Color.White, Color.Transparent))
            is Loaded -> largeRadialGradient(listOf(Sunny, Color.Transparent))
            is Error -> largeRadialGradient(
                listOf(
                    Color.Red.copy(alpha = 0.7f),
                    Color.Transparent
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background),
            contentAlignment = TopEnd
        ) {
            when (uiState.loadState) {
                is Loading -> LoadingView("Loading...").run {
                    viewModel.timerFired()
                }
                is Error -> Error((uiState.loadState as Error).reason) { coroutineScope.launch { viewModel.timerFired() } }
                is Loaded -> Loaded(viewModel, (uiState.loadState as Loaded).viewModel, themeStream)
            }

            if (configManager.isDemoUser) {
                OutlinedCard(
                    colors = outlinedCardColors(containerColor = Color.Red),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        "Demo",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                }
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
        homePowerFlowViewModel: SummaryPowerFlowViewModel,
        themeStream: MutableStateFlow<AppTheme>
    ) {
        val updateMessage by viewModel.updateMessage.collectAsState()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SummaryPowerFlowView().Content(
                modifier = Modifier.weight(1f),
                viewModel = homePowerFlowViewModel,
                themeStream = themeStream
            )

            Text(
                updateMessage ?: "",
                color = Color.Gray,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .padding(bottom = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
fun PowerFlowTabViewPreview() {
    val viewModel = PowerFlowTabViewModel(DemoNetworking(), FakeConfigManager(), RawDataStore())
    val homePowerFlowViewModel = SummaryPowerFlowViewModel(
        FakeConfigManager(),
        2.3,
        0.5,
        0.03,
        0.0,
        0.4,
        true
    )

    EnergyStatsTheme {
//        PowerFlowTabView(
//            DemoNetworking(),
//            FakeConfigManager()
//        ).Content(viewModel)
        PowerFlowTabView(
            DemoNetworking(),
            FakeConfigManager(),
            RawDataStore()
        ).Loaded(
            viewModel = viewModel,
            homePowerFlowViewModel = homePowerFlowViewModel,
            themeStream = MutableStateFlow(AppTheme.UseDefaultDisplay)
        )
    }
}

data class UiState(
    val loadState: LoadState
)

sealed class LoadState
class Error(val reason: String) : LoadState()
object Loading : LoadState()
class Loaded(val viewModel: SummaryPowerFlowViewModel) : LoadState()
