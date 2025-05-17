package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.asPercent
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.models.power
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.settings.inverter.CT2DisplayMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.PowerFlowNeutralText
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SolarStringsView(themeStream: MutableStateFlow<AppTheme>, viewModel: LoadedPowerFlowViewModel) {
    val theme by themeStream.collectAsState()
    val displayStrings = viewModel.displayStrings.collectAsState().value
    val todaysGeneration = viewModel.todaysGeneration.collectAsState().value
    val showPercentage = remember { mutableStateOf(false) }

    if ((theme.ct2DisplayMode == CT2DisplayMode.AsPowerString || theme.powerFlowStrings.enabled) && displayStrings.isNotEmpty()) {
        Column(
            modifier = Modifier
                .offset(y = -(displayStrings.count() * 10).dp)
                .background(Color.LightGray)
                .padding(2.dp)
                .clickable { showPercentage.value = !showPercentage.value }
        ) {
            displayStrings.forEach {
                Row {
                    Text(
                        it.displayName(theme.powerFlowStrings),
                        Modifier.padding(end = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = PowerFlowNeutralText
                    )
                    Text(
                        it.amount.power(theme.displayUnit, theme.decimalPlaces),
                        style = MaterialTheme.typography.bodySmall,
                        color = PowerFlowNeutralText
                    )

                    if (theme.totalYieldModel != TotalYieldModel.Off) {
                        todaysGeneration?.let { viewModel ->
                            val amount: String = if (showPercentage.value) {
                                viewModel.estimatedTotalPercentage(it.stringType()).asPercent()
                            } else {
                                viewModel.estimatedTotalEnergy(it.stringType()).energy(theme.displayUnit, 1)
                            }

                            Text(
                                text = stringResource(R.string.string_total_today_description, amount),
                                color = PowerFlowNeutralText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}