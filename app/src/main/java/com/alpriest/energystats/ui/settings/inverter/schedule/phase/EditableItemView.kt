package com.alpriest.energystats.ui.settings.inverter.schedule.phase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.settings.InfoButton
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild

@Composable
fun EditableItemView(
    value: String,
    errorText: String?,
    title: String,
    unit: String?,
    description: String?,
    onValueChange: (String) -> Unit
) {
    SettingsColumnWithChild(
        error = errorText
    ) {
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            modifier = Modifier.Companion
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = Modifier.Companion.weight(1f)
            ) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                description?.let {
                    InfoButton(it)
                }
            }

            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(it.filter { it.isDigit() }) },
                modifier = Modifier.Companion
                    .padding(start = 8.dp)
                    .width(120.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Companion.End, color = MaterialTheme.colorScheme.onSecondary),
                trailingIcon = { unit?.let { Text(it, color = MaterialTheme.colorScheme.onSecondary) } },
                singleLine = true
            )
        }
    }
}