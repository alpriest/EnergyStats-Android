package com.alpriest.energystats.ui.settings.battery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.shared.models.TimeType
import com.alpriest.energystats.shared.models.network.Time
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.anhaki.picktime.PickHourMinute
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePeriodView(
    time: Time,
    timeType: TimeType,
    title: String,
    labelStyle: TextStyle,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    includeSeconds: Boolean,
    timeTypeShowing: MutableState<TimeType?>,
    onChange: (Time) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    if (timeTypeShowing.value != timeType) {
                        timeTypeShowing.value = timeType
                    } else {
                        timeTypeShowing.value = null
                    }
                }
                .padding(vertical = 8.dp)
        ) {
            Text(
                title,
                style = labelStyle,
                color = MaterialTheme.colorScheme.onSecondary,
            )

            Text(
                "${"%02d".format(time.hour)}:${"%02d".format(time.minute)}" + (if (includeSeconds) ":" + timeType.appendage() else ""),
                style = textStyle,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }

        if (timeTypeShowing.value == timeType) {
            PickHourMinute(
                time.hour,
                onHourChange = { hour ->
                    onChange(time.copy(hour = hour))
                },
                time.minute,
                onMinuteChange = { minute ->
                    onChange(time.copy(minute = minute))
                },
                selectedTextStyle = PickTimeTextStyle(
                    color = Color(0xFF29778E),
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                ),
                focusIndicator = PickTimeFocusIndicator(
                    enabled = true,
                    widthFull = false,
                    background = Color(0xFFCCEDF9),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, Color(0xFF87CDE6)),
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun TimePeriodViewPreview() {
    EnergyStatsTheme {
        TimePeriodView(
            time = Time(1, 30),
            timeType = TimeType.START,
            title = "Hello",
            labelStyle = TextStyle.Default,
            includeSeconds = false,
            timeTypeShowing = mutableStateOf(TimeType.END),
            onChange = {}
        )
    }
}
