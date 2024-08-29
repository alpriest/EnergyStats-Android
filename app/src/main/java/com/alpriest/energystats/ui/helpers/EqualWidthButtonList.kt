package com.alpriest.energystats.ui.helpers

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class ButtonDefinition(val title: String, val onClick: () -> Unit)

@Composable
fun EqualWidthButtonList(buttons: List<ButtonDefinition>) {
    val maxIntrinsicButtonWidth = remember {
        buttons.maxOf { it.title.length * 9 }
    }

    buttons.forEach {
        Button(
            onClick = it.onClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                it.title,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.width(maxIntrinsicButtonWidth.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}