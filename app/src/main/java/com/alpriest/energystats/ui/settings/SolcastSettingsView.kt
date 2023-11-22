package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.ui.ClickableUrlText

@Composable
fun SolcastSettingsView() {
    var password by remember { mutableStateOf("") }
    val text = "Your API key can be found at https://toolkit.solcast.com.au by clicking the menu at the top right of the page."

    SettingsColumnWithChild {
        ClickableUrlText(
            text = "Solcast provide solar predictions based on your location. To sign up for free, visit https://solcast.com/free-rooftop-solar-forecasting and register for Hobbyist Access.\n" +
                    "\n" +
                    "Once you've signed up and have created your site(s) paste your API key below.",
            style = MaterialTheme.typography.body2
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = { password = it },
            label = { Text("API Key") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        ClickableUrlText(
            text = text,
            style = MaterialTheme.typography.caption
        )
    }
}

@Preview
@Composable
fun SolcastSettingsViewPreview() {
    SolcastSettingsView()
}