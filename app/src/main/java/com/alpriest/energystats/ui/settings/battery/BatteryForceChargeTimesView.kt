package com.alpriest.energystats.ui.settings.battery

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun BatteryForceChargeTimesView() {

}

@Composable
fun BatteryTimePeriodView() {
    val enabled = rememberSaveable { mutableStateOf(false) }
    var startHour = rememberSaveable { mutableStateOf(0) }
    val startMin = rememberSaveable { mutableStateOf(0) }
    val startDialog = TimePickerDialog(LocalContext.current,
        { _, mHour: Int, mMinute: Int ->
            startHour.value = mHour
            startMin.value = mMinute
        },
        startHour.value,
        startMin.value,
        false
    )

    Column {
        Text("Period 1")

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

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start")
            TimePickerDialog(LocalContext.current,
                { _, mHour: Int, mMinute: Int ->
                    startHour.value = mHour
                    startMin.value = mMinute
                },
                startHour.value,
                startMin.value,
                false
            )

            Text(
                "${"%02d".format(startHour.value)}:${"%02d".format(startMin.value)}",
                modifier = Modifier.clickable {
                    startDialog.show()
                }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 300)
@Composable
fun BatteryTimePeriodViewPreview() {
    EnergyStatsTheme {
        BatteryTimePeriodView()
    }
}
