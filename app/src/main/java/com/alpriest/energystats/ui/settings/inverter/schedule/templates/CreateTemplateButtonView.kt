package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun CreateTemplateButtonView(content: @Composable RowScope.() -> Unit, onDismiss: (String) -> Unit) {
    val show = remember { mutableStateOf(false) }

    Button(
        onClick = { show.value = true },
    ) {
        content()
    }

    if (show.value) {
        TemplateNameAlertDialog {
            show.value = false
            it?.let {
                onDismiss(it)
            }
        }
    }
}

@Composable
private fun TemplateNameAlertDialog(onDismiss: (String?) -> Unit) {
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismiss(null) }) {
        Card(modifier = Modifier.padding(vertical = 24.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Enter template name")
                OutlinedTextField(value = name.value, onValueChange = { name.value = it })

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { onDismiss(name.value) }) {
                        Text(context.getString(R.string.ok))
                    }
                    Button(onClick = { onDismiss(null) }) {
                        Text(context.getString(R.string.cancel))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun CreateTemplateButtonViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        CreateTemplateButtonView(
            content = { Text("Create new template") },
            onDismiss = { }
        )
    }
}
