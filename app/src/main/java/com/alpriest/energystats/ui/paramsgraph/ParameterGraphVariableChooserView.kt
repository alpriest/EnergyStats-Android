package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.ui.settings.RoundedColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun ParameterGraphVariableChooserView(viewModel: ParameterGraphVariableChooserViewModel, onCancel: () -> Unit) {
    val scrollState = rememberScrollState()
    val variables = viewModel.variablesState.collectAsState().value
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 44.dp)
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 94.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            RoundedColumnWithChild(modifier = Modifier.padding(top = 12.dp)) {
                SettingsTitleView(stringResource(id = R.string.predefined_selections))

                OutlinedButton(onClick = { viewModel.chooseDefaultVariables() }) { Text(stringResource(R.string.defalt)) }
                OutlinedButton(onClick = { viewModel.chooseCompareStringsValues() }) { Text(stringResource(R.string.compare_strings)) }
                OutlinedButton(onClick = { viewModel.chooseTemperaturesVariables() }) { Text(stringResource(R.string.temperatures)) }
                OutlinedButton(onClick = { viewModel.chooseBatteryVariables() }) { Text(stringResource(R.string.battery)) }
                OutlinedButton(onClick = { viewModel.chooseNoVariables() }) { Text(stringResource(R.string.none)) }
            }

            RoundedColumnWithChild(modifier = Modifier.padding(bottom = 12.dp)) {
                SettingsTitleView(stringResource(id = R.string.all))

                variables.forEach { variable ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { viewModel.toggle(variable) }
                            .fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = variable.isSelected,
                            onCheckedChange = {
                                viewModel.toggle(variable)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                        )
                        Text(
                            variable.type.name,
                            modifier = Modifier.weight(0.5f)
                        )

                        Text(
                            variable.type.unit,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Button(
                    onClick = {
                        uriHandler.openUri("https://github.com/TonyM1958/HA-FoxESS-Modbus/wiki/Fox-ESS-Cloud#search-parameters")
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colors.primary,
                        backgroundColor = Color.Transparent
                    ),
                    elevation = null,
                ) {
                    Icon(
                        Icons.Default.OpenInBrowser, contentDescription = "Open In Browser", modifier = Modifier.padding(end = 5.dp)
                    )
                    Text(
                        stringResource(R.string.find_out_more_about_these_variables),
                        fontSize = 12.sp,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onCancel() }) {
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.apply()
                        onCancel()
                    }
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
            Text(stringResource(R.string.note_that_not_all_parameters_contain_values), modifier = Modifier.align(CenterHorizontally))
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun ParameterGraphVariableChooserViewPreview() {
    val variables = listOf(
        RawVariable("PV1Volt", "pv1Volt", "V"),
//        RawVariable("PV1Current", "pv1Current", "A"),
//        RawVariable("PV1Power", "pv1Power", "kW"),
//        RawVariable("PVPower", "pvPower", "kW"),
//        RawVariable("PV2Volt", "pv2Volt", "V"),
//        RawVariable("PV2Current", "pv2Current", "A"),
//        RawVariable("aPV1Current", "pv1Current", "A"),
//        RawVariable("aPV1Power", "pv1Power", "kW"),
//        RawVariable("aPVPower", "pvPower", "kW"),
//        RawVariable("aPV2Volt", "pv2Volt", "V"),
//        RawVariable("aPV2Current", "pv2Current", "A"),
//        RawVariable("bPV1Current", "pv1Current", "A"),
//        RawVariable("bPV1Power", "pv1Power", "kW"),
//        RawVariable("bPVPower", "pvPower", "kW"),
//        RawVariable("bPV2Volt", "pv2Volt", "V"),
//        RawVariable("cPV2Current", "pv2Current", "A"),
//        RawVariable("cPV1Current", "pv1Current", "A"),
//        RawVariable("cPV1Power", "pv1Power", "kW"),
//        RawVariable("cPVPower", "pvPower", "kW"),
//        RawVariable("cPV2Volt", "pv2Volt", "V"),
//        RawVariable("dPV2Current", "pv2Current", "A"),
        RawVariable("dPV2Power", "pv2Power", "kW")
    ).map { variable ->
        ParameterGraphVariable(variable, isSelected = listOf(true, false).random(), enabled = true)
    }

    EnergyStatsTheme {
        ParameterGraphVariableChooserView(
            viewModel = ParameterGraphVariableChooserViewModel(variables, onApply = { }),
            onCancel = {}
        )
    }
}