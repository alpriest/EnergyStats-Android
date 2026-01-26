package com.alpriest.energystats.ui.paramsgraph

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.models.GraphBounds
import com.alpriest.energystats.shared.models.Variable
import com.alpriest.energystats.shared.helpers.kW
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.network.DemoNetworking
import com.alpriest.energystats.shared.models.ValueUsage
import com.alpriest.energystats.ui.ToggleRowView
import com.alpriest.energystats.ui.paramsgraph.editing.previewParameterGraphVariables
import com.alpriest.energystats.ui.statsgraph.title
import com.alpriest.energystats.ui.summary.DemoSolarForecasting
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.demo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@SuppressLint("DiscouragedApi")
@Composable
fun ParameterGraphVariableTogglesView(viewModel: ParametersGraphTabViewModel, unit: String?, appSettingsStream: StateFlow<AppSettings>, modifier: Modifier = Modifier) {
    val graphVariables = viewModel.graphVariablesStream.collectAsState()
    val selectedValues = viewModel.valuesAtTimeStream.collectAsState().value
    val boundsValues = viewModel.boundsStream.collectAsState().value
    val appSettings = appSettingsStream.collectAsState().value
    val context = LocalContext.current

    Column(modifier) {
        graphVariables.value
            .filter { it.isSelected }
            .filter { unit == null || it.type.unit == unit }
            .map {
                val selectedValue = selectedValues
                    .filter { it.key.unit == unit }
                    .map { it.value }
                    .flatten()
                    .firstOrNull { entry -> entry.type == it.type }
                val titleType = ValueUsage.SNAPSHOT

                val title = when (it.type.variable) {
                    "generationPower" -> stringResource(R.string.output) + title(titleType)
                    "feedinPower" -> stringResource(R.string.feed_in) + title(titleType)
                    "batChargePower" -> stringResource(R.string.charge) + title(titleType)
                    "batDischargePower" -> stringResource(R.string.discharge) + title(titleType)
                    "gridConsumptionPower" -> stringResource(R.string.grid_consumption) + title(titleType)
                    "loadsPower" -> stringResource(R.string.loads) + title(titleType)
                    else -> it.type.name
                }

                val id = context.resources.getIdentifier("rawvariable_${it.type.variable.lowercase()}", "string", LocalContext.current.applicationInfo.packageName)
                val description: String? = if (id > 0) {
                    stringResource(id)
                } else {
                    null
                }

                if (selectedValue == null) {
                    val boundsValue = boundsValues.firstOrNull { entry -> entry.type == it.type }
                    val graphBounds = boundsValue?.let { GraphBounds(it.min, it.max, it.now) }
                    ToggleRowView(it, appSettingsStream, { viewModel.toggleVisibility(it, unit) }, title, description, null, graphBounds)
                } else {
                    val formattedValue = when (it.type.unit) {
                        "kW" -> selectedValue.y.toDouble().kW(appSettings.decimalPlaces)
                        else -> "${selectedValue.y} ${selectedValue.type.unit}"
                    }
                    ToggleRowView(it, appSettingsStream, { viewModel.toggleVisibility(it, unit) }, title, description, formattedValue, null)
                }
            }
    }
}

@Composable
@Preview(widthDp = 340)
fun ParameterGraphVariableTogglesViewPreview() {
    ParameterGraphVariableTogglesView(
        ParametersGraphTabViewModel(
            DemoNetworking(),
            FakeConfigManager(),
            onWriteTempFile = { _, _ -> null },
            MutableStateFlow(previewParameterGraphVariables()),
            solarForecastProvider = { DemoSolarForecasting() }
        ),
        null,
        appSettingsStream = MutableStateFlow(AppSettings.demo(useLargeDisplay = false))
    )
}
