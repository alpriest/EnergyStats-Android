package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.editing.previewParameterGraphVariables
import com.alpriest.energystats.ui.statsgraph.CalendarView
import com.alpriest.energystats.ui.summary.DemoSolarForecasting
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate

@Composable
fun ParameterGraphHeaderView(viewModel: ParametersGraphTabViewModel, modifier: Modifier = Modifier, navController: NavHostController, configManager: ConfigManaging) {
    var hours by remember { mutableStateOf(viewModel.displayModeStream.value.hours) }
    val candidateQueryDate = MutableStateFlow(viewModel.displayModeStream.collectAsState().value.date)
    var hoursButtonEnabled by remember { mutableStateOf(true) }
    val currentDate = remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000) // Check once per minute
            currentDate.value = LocalDate.now()
        }
    }

    LaunchedEffect(candidateQueryDate) {
        candidateQueryDate
            .onEach {
                viewModel.displayModeStream.value = ParametersDisplayMode(it, hours)
                hoursButtonEnabled = it == LocalDate.now()
            }
            .collect {}
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(start = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        MenuWithButton(
            modifier = Modifier.padding(end = 14.dp),
            Icons.AutoMirrored.Filled.FormatListBulleted
        ) { showing ->
            DropdownMenuItem(onClick = {
                navController.navigate(ParametersScreen.ParameterChooser.name)
            }, text = {
                Text(stringResource(R.string.parameters))
            })

            HorizontalDivider(thickness = 5.dp)

            HourPickerItems(hours, showing, onHoursChanged = {
                hours = it
                viewModel.displayModeStream.value = ParametersDisplayMode(candidateQueryDate.value, hours)
            })

            HorizontalDivider(thickness = 5.dp)

            DropdownMenuItem(onClick = {
                configManager.truncatedYAxisOnParameterGraphs = !configManager.truncatedYAxisOnParameterGraphs
                showing.value = false
            }, text = {
                Text(stringResource(R.string.display_truncated_y_axis))
            }, trailingIcon = {
                if (configManager.truncatedYAxisOnParameterGraphs) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                }
            })
        }

        CalendarView(dateStream = candidateQueryDate, style = Typography.headlineMedium)

        Spacer(modifier = Modifier.weight(2.0f))

        ESButton(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .padding(end = 14.dp)
                .size(36.dp),
            onClick = {
                hours = 24
                candidateQueryDate.value = candidateQueryDate.value.minusDays(1)
            },
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Left", modifier = Modifier.size(32.dp))
        }

        val date = candidateQueryDate.collectAsState().value
        ESButton(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(36.dp),
            onClick = {
                hours = 24
                candidateQueryDate.value = candidateQueryDate.value.plusDays(1)
            },
            contentPadding = PaddingValues(0.dp),
            enabled = date.atStartOfDay() < currentDate.value.atStartOfDay()
        ) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Right", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun MenuWithButton(modifier: Modifier = Modifier, icon: ImageVector, content: @Composable() (ColumnScope.(MutableState<Boolean>) -> Unit)) {
    val showing = remember { mutableStateOf(false) }

    Box(modifier) {
        ESButton(
            onClick = { showing.value = true },
            modifier = Modifier
                .padding(vertical = 4.dp)
                .size(36.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }

        DropdownMenu(expanded = showing.value, onDismissRequest = { showing.value = false }) {
            content(showing)
        }
    }
}

@Composable
private fun HourPickerItems(hours: Int, showing: MutableState<Boolean>, onHoursChanged: (Int) -> Unit) {
    DropdownMenuItem(onClick = {
        onHoursChanged(6)
        showing.value = false
    }, text = {
        Text("6 hours")
    }, trailingIcon = {
        if (hours == 6) {
            Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
        }
    })
    HorizontalDivider()

    DropdownMenuItem(onClick = {
        onHoursChanged(12)
        showing.value = false
    }, text = {
        Text("12 hours")
    }, trailingIcon = {
        if (hours == 12) {
            Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
        }
    })
    HorizontalDivider()

    DropdownMenuItem(onClick = {
        onHoursChanged(24)
        showing.value = false
    }, text = {
        Text("24 hours")
    }, trailingIcon = {
        if (hours == 24) {
            Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
        }
    })
}

@Composable
@Preview(heightDp = 400)
fun ParameterGraphHeaderViewPreview() {
    ParameterGraphHeaderView(
        ParametersGraphTabViewModel(
            DemoNetworking(),
            FakeConfigManager(),
            onWriteTempFile = { _, _ -> null },
            MutableStateFlow(previewParameterGraphVariables()),
            solarForecastProvider = { DemoSolarForecasting() }
        ),
        navController = NavHostController(LocalContext.current),
        configManager = FakeConfigManager(),
    )
}
