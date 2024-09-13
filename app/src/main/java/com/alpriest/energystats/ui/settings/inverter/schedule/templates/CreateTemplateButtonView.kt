package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

enum class AlertConfiguration {
    DuplicateTemplate,
    CreateTemplate,
    RenameTemplate;

    val title: String
        get() = when (this) {
            CreateTemplate -> "Create template"
            DuplicateTemplate -> "Duplicate template"
            RenameTemplate -> "Rename template"
        }

    val actionButton: String
        get() = when (this) {
            CreateTemplate -> "Create"
            DuplicateTemplate -> "Duplicate"
            RenameTemplate -> "Rename"
        }
}

@Composable
fun TemplateNameAlertDialog(configuration: AlertConfiguration, onDismiss: (String?) -> Unit) {
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismiss(null) }) {
        Card(modifier = Modifier.padding(vertical = 24.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(configuration.title)
                OutlinedTextField(value = name.value, onValueChange = { name.value = it })

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { onDismiss(null) }) {
                        Text(context.getString(R.string.cancel))
                    }
                    Button(onClick = { onDismiss(name.value) }) {
                        Text(configuration.actionButton)
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
        TemplateNameAlertDialog(
            configuration = AlertConfiguration.RenameTemplate,
            onDismiss = { }
        )
    }
}
