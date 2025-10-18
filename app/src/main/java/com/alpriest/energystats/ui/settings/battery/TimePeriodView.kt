package com.alpriest.energystats.ui.settings.battery

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import com.alpriest.energystats.models.Time

@Composable
fun TimePeriodView(
    time: Time,
    timeType: TimeType,
    title: String,
    labelStyle: TextStyle,
    textStyle: TextStyle = TextStyle.Companion.Default,
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
        verticalAlignment = Alignment.Companion.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            title,
            style = labelStyle,
            color = MaterialTheme.colorScheme.onSecondary,
        )

        Text(
            "${"%02d".format(time.hour)}:${"%02d".format(time.minute)}" + (if (includeSeconds) ":" + timeType.appendage() else ""),
            style = textStyle,
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.Companion.clickable {
                dialog.show()
            })
    }
}