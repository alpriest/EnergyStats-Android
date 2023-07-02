package com.alpriest.energystats.ui.paramsgraph

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.ValueUsage
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.ToggleRowView
import com.alpriest.energystats.ui.statsgraph.title
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@SuppressLint("DiscouragedApi")
@Composable
fun ParameterGraphVariableTogglesView(viewModel: ParametersGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val graphVariables = viewModel.graphVariablesStream.collectAsState()
    val selectedValues = viewModel.valuesAtTimeStream.collectAsState().value
    val selectedDateTime = selectedValues.firstOrNull()?.localDateTime
    val boundsValues = viewModel.boundsStream.collectAsState().value

    Column(modifier) {
        graphVariables.value.filter { it.isSelected }.map {
            val selectedValue = selectedValues.firstOrNull { entry -> entry.type == it.type }
            val titleType = if (selectedValue == null) ValueUsage.TOTAL else ValueUsage.SNAPSHOT

            val title = when (it.type.variable) {
                "generationPower" -> stringResource(R.string.output) + title(titleType)
                "feedinPower" -> stringResource(R.string.feed_in) + title(titleType)
                "batChargePower" -> stringResource(R.string.charge) + title(titleType)
                "batDischargePower" -> stringResource(R.string.discharge) + title(titleType)
                "gridConsumptionPower" -> stringResource(R.string.grid_consumption) + title(titleType)
                "loadsPower" -> stringResource(R.string.loads) + title(titleType)
                else -> it.type.name
            }

            val id = LocalContext.current.getResources().getIdentifier("rawvariable_${it.type.variable.lowercase()}", "string", LocalContext.current.applicationInfo.packageName)
            val description: String = if (id > 0) { stringResource(id) } else { it.type.variable }

            selectedValue?.let { entry ->
                ToggleRowView(it, themeStream, { viewModel.toggleVisibility(it) }, title, description, entry.y?.toDouble(), null)
            } ?: run {
                val boundsValue = boundsValues.firstOrNull { entry -> entry.type == it.type }
                ToggleRowView(it, themeStream, { viewModel.toggleVisibility(it) }, title, description, null, boundsValue)
            }
        }
    }

    selectedDateTime?.let {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
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
    ParameterGraphVariableTogglesView(
        ParametersGraphTabViewModel(FakeConfigManager(), DemoNetworking(), onWriteTempFile = { _, _ -> null }),
        themeStream = MutableStateFlow(AppTheme.preview(useLargeDisplay = false))
    )
}
