package com.alpriest.energystats.ui.helpers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.settings.SlimButton
import com.alpriest.energystats.ui.theme.Typography
import java.util.Calendar

@Composable
fun YearPicker(year: Int, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: (Int) -> Unit) {
    var showing by remember { mutableStateOf(false) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopStart)
            .padding(end = 14.dp)
    ) {
        SlimButton(
            enabled = enabled,
            onClick = { showing = true }
        ) {
            Text(year.toString(), style = Typography.headlineMedium)
        }

        DropdownMenu(expanded = showing, onDismissRequest = { showing = false }) {
            for (yearIndex in 2021..currentYear) {
                DropdownMenuItem(onClick = {
                    onClick(yearIndex)
                    showing = false
                }, text = {
                    Text(yearIndex.toString())
                }, trailingIcon = {
                    if (yearIndex == year) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                    }
                })
                if (yearIndex < currentYear) {
                    HorizontalDivider()
                }
            }
        }
    }
}