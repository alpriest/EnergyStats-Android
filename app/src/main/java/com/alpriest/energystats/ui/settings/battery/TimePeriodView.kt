package com.alpriest.energystats.ui.settings.battery

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.shared.models.TimeType
import com.alpriest.energystats.shared.models.network.Time

@Composable
fun TimePeriodView(
    time: Time,
    timeType: TimeType,
    title: String,
    labelStyle: TextStyle,
    textStyle: TextStyle = TextStyle.Default,
    includeSeconds: Boolean,
    modifier: Modifier = Modifier.Companion,
    onChange: (Int, Int) -> Unit
) {
    val dialog = TimePickerDialog(
        LocalContext.current, { _, mHour: Int, mMinute: Int ->
            onChange(mHour, mMinute)
        },
        time.hour,
        time.minute,
        true
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth().clickable {
            dialog.show()
        }.padding(vertical = 8.dp)
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
}