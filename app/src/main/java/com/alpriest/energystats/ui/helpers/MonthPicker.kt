package com.alpriest.energystats.ui.helpers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.settings.OutlinedSlimButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Typography
import java.time.Month
import java.util.Locale

@Composable
fun MonthPicker(month: Int, modifier: Modifier = Modifier, textModifier: Modifier = Modifier, onPrimary: Boolean, onClick: (Int) -> Unit) {
    var showing by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopStart)
            .padding(end = 14.dp)
    ) {
        OutlinedSlimButton(
            onClick = { showing = true },
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = Color.Transparent,
                contentColor = if (onPrimary) colorScheme.onPrimary else colorScheme.primary
            ),
            content = {
                val selectedMonth = Month.of(month.coerceIn(1..<12))
                Row(modifier = textModifier) {
                    Text(
                        selectedMonth.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()),
                        style = Typography.headlineMedium
                    )
                    Icon(Icons.Default.ArrowDropDown, "down")
                }
            },
        )

        DropdownMenu(expanded = showing, onDismissRequest = { showing = false }) {
            for (monthIndex in 1 until 13) {
                val selectedMonth = Month.of(monthIndex)
                DropdownMenuItem(onClick = {
                    onClick(monthIndex)
                    showing = false
                }, text = {
                    Text(
                        selectedMonth.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
                    )
                }, trailingIcon = {
                    if (monthIndex == month) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                    }
                })
                if (monthIndex < 12) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
private fun MonthPickerViewPreview() {
    EnergyStatsTheme {
        MonthPicker(
            1,
            onPrimary = false,
            onClick = { _ -> }
        )
    }
}
