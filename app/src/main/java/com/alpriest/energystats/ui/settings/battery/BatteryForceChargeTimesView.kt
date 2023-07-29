package com.alpriest.energystats.ui.settings.battery

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Divider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.alpriest.energystats.ui.settings.SettingsButton
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BatteryForceChargeTimesViewModel : ViewModel() {
    val startHourStream = MutableStateFlow(0)
    val startMinuteStream = MutableStateFlow(0)
    val endHourStream = MutableStateFlow(0)
    val endMinuteStream = MutableStateFlow(0)

    suspend fun load() {}
    suspend fun save() {}
}

@Composable
fun BatteryForceChargeTimesView(viewModel: BatteryForceChargeTimesViewModel) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        BatteryTimePeriodView(viewModel, "Period 1")
        BatteryTimePeriodView(viewModel, "Period 2")

        SettingsButton("Save") {
            coroutineScope.launch {
                viewModel.save()
            }
        }
    }
}

@Composable
fun BatteryTimePeriodView(viewModel: BatteryForceChargeTimesViewModel, periodTitle: String) {
    val enabled = rememberSaveable { mutableStateOf(false) }

    SettingsColumnWithChild {
        Text(periodTitle)

        Divider()

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enable charge from grid")
            Switch(
                checked = enabled.value,
                onCheckedChange = { enabled.value = it }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))
        TimePeriodView(viewModel.startHourStream, viewModel.startMinuteStream, "Start")
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        TimePeriodView(viewModel.endHourStream, viewModel.endMinuteStream, "End")
    }
}

@Composable
fun TimePeriodView(hourStream: MutableStateFlow<Int>, minuteStream: MutableStateFlow<Int>, title: String) {
    val hour = hourStream.collectAsState().value
    val minute = minuteStream.collectAsState().value

    val dialog = TimePickerDialog(
        LocalContext.current,
        { _, mHour: Int, mMinute: Int ->
            hourStream.value = mHour
            minuteStream.value = mMinute
        },
        hour,
        minute,
        false
    )

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(title)
        TimePickerDialog(
            LocalContext.current,
            { _, mHour: Int, mMinute: Int ->
                hourStream.value = mHour
                minuteStream.value = mMinute
            },
            hour,
            minute,
            false
        )

        Text(
            "${"%02d".format(hour)}:${"%02d".format(minute)}",
            modifier = Modifier.clickable {
                dialog.show()
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 300)
@Composable
fun BatteryForceChargeTimesViewPreview() {
    EnergyStatsTheme {
        BatteryForceChargeTimesView(viewModel = BatteryForceChargeTimesViewModel())
    }
}
