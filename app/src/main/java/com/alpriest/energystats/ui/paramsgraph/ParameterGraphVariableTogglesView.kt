package com.alpriest.energystats.ui.paramsgraph

import android.content.res.Resources
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.ToggleRowView
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.DateFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ParameterGraphVariableTogglesView(viewModel: ParametersGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val graphVariables = viewModel.graphVariablesStream.collectAsState()
    val selectedValues = viewModel.valuesAtTimeStream.collectAsState().value
    val selectedDateTime = selectedValues.firstOrNull()?.localDateTime

    Column(modifier) {
        graphVariables.value.filter { it.isSelected }.map {
            val title = when (it.type.variable) {
                "generationPower" -> "Output energy"
                "feedinPower" -> "Feed-in energy"
                "batChargePower" -> "Charge energy"
                "batDischargePower" -> "Discharge energy"
                "gridConsumptionPower" -> "Grid consumption energy"
                "loadsPower" -> "Loads energy"
                else -> it.type.name
            }

            val description = when (it.type.variable) {
                "generationPower" -> stringResource(R.string.rawvariable_generationpower)
                "feedInPower" -> stringResource(R.string.rawvariable_feedinpower)
                "batChargePower" -> stringResource(R.string.rawvariable_batchargepower)
                "batDischargePower" -> stringResource(R.string.rawvariable_batdischargepower)
                "gridConsumptionPower" -> stringResource(R.string.rawvariable_gridconsumptionpower)
                "loadsPower" -> stringResource(R.string.rawvariable_loadspower)
                else -> it.type.name
            }

            val value = selectedValues.firstOrNull { entry -> entry.type == it.type }

            ToggleRowView(it, themeStream, { viewModel.toggleVisibility(it) }, title, description, value?.y?.toDouble())
        }
    }

    selectedDateTime?.let {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp).fillMaxWidth()
        ) {
            Text(
                text = it.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
            )
        }
    }
}

@Composable
@Preview(widthDp = 340)
fun ParameterGraphVariableTogglesViewPreview() {
    ParameterGraphVariableTogglesView(ParametersGraphTabViewModel(FakeConfigManager(), DemoNetworking()), themeStream = MutableStateFlow(AppTheme.preview(useLargeDisplay = false)))
}
