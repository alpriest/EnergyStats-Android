package com.alpriest.energystats.ui.paramsgraph.editing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable
import com.alpriest.energystats.ui.paramsgraph.ParametersScreen
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

class ParameterGraphVariableChooserView(
    private val configManager: ConfigManaging,
    private val variables: MutableStateFlow<List<ParameterGraphVariable>>,
    private val navController: NavHostController
) {
    @Composable
    fun Content(
        viewModel: ParameterGraphVariableChooserViewModel = viewModel(factory = ParameterGraphVariableChooserViewModelFactory(configManager, variables))
    ) {
        val scrollState = rememberScrollState()
        val variables = viewModel.variablesState.collectAsState().value
        val uriHandler = LocalUriHandler.current
        val groups = configManager.themeStream.collectAsState().value.parameterGroups

        ContentWithBottomButtons(navController, onSave = {
            viewModel.apply()
            navController.popBackStack()
        }, footer = {
            Text(
                stringResource(R.string.note_that_not_all_parameters_contain_values),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }, content = { modifier ->
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Box {
                    SettingsColumnWithChild(modifier = Modifier.padding(top = 12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Groups",
                                style = MaterialTheme.typography.h4,
                                color = colors.onSecondary,
                                modifier = Modifier.weight(1.0f)
                            )
                        }

                        listRow(onClick = { viewModel.chooseDefaultVariables() }) { Text(stringResource(R.string.defalt)) }
                        Divider()
                        listRow(onClick = { viewModel.chooseNoVariables() }) { Text(stringResource(R.string.none)) }
                        Divider()

                        groups.forEachIndexed { index, item ->
                            Row {
                                listRow(
                                    onClick = { viewModel.select(item.parameterNames) }
                                ) {
                                    Text(item.title)
                                }
                            }

                            if (index < groups.lastIndex) {
                                Divider()
                            }
                        }
                    }

                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        Spacer(modifier = Modifier.weight(1.0f))
                        IconButton(onClick = { navController.navigate(ParametersScreen.ParameterGroupEditor.name) }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = colors.onSecondary,
                            )
                        }
                    }
                }

                SettingsColumnWithChild(modifier = Modifier.padding(bottom = 12.dp)) {
                    SettingsTitleView(stringResource(id = R.string.all))

                    ParameterVariableListView(variables, onTap = { viewModel.toggle(it) })
                }

                Column(
                    modifier = Modifier.fillMaxWidth(), horizontalAlignment = CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            uriHandler.openUri("https://github.com/TonyM1958/HA-FoxESS-Modbus/wiki/Fox-ESS-Cloud#search-parameters")
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = colors.primary, backgroundColor = Color.Transparent
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
        })
    }
}

@Composable
fun listRow(onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun ParameterGraphVariableChooserViewPreview() {
    EnergyStatsTheme {
        ParameterGraphVariableChooserView(
            FakeConfigManager(),
            MutableStateFlow(previewParameterGraphVariables()),
            NavHostController(LocalContext.current)
        ).Content()
    }
}

fun previewParameterGraphVariables(): List<ParameterGraphVariable> {
    return listOf(
        RawVariable("PV1Volt", "pv1Volt", "V"),
        RawVariable("PV1Current", "pv1Current", "A"),
        RawVariable("PV1Power", "pv1Power", "kW"),
        RawVariable("PVPower", "pvPower", "kW"),
        RawVariable("PV2Volt", "pv2Volt", "V"),
        RawVariable("PV2Current", "pv2Current", "A"),
        RawVariable("aPV1Current", "pv1Current", "A"),
        RawVariable("aPV1Power", "pv1Power", "kW"),
        RawVariable("aPVPower", "pvPower", "kW"),
        RawVariable("aPV2Volt", "pv2Volt", "V"),
        RawVariable("aPV2Current", "pv2Current", "A"),
        RawVariable("bPV1Current", "pv1Current", "A"),
        RawVariable("bPV1Power", "pv1Power", "kW"),
        RawVariable("bPVPower", "pvPower", "kW"),
        RawVariable("bPV2Volt", "pv2Volt", "V"),
        RawVariable("cPV2Current", "pv2Current", "A"),
        RawVariable("cPV1Current", "pv1Current", "A"),
        RawVariable("cPV1Power", "pv1Power", "kW"),
        RawVariable("cPVPower", "pvPower", "kW"),
        RawVariable("cPV2Volt", "pv2Volt", "V"),
        RawVariable("dPV2Current", "pv2Current", "A"),
        RawVariable("dPV2Power", "pv2Power", "kW")
    ).map { variable ->
        ParameterGraphVariable(variable, isSelected = listOf(true, false).random(), enabled = true)
    }
}