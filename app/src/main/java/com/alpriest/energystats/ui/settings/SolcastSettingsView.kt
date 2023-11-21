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

@Composable
fun SolcastSettingsView() {
    var password by remember { mutableStateOf("") }

    SettingsColumnWithChild {
        Text(
            "Solcast provide solar predictions based on your location. To sign up for free, visit https://solcast.com/free-rooftop-solar-forecasting and register for Hobbyist Access.\n" +
                    "\n" +
                    "Once you've signed up and have created your site(s) paste your API key below."
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = { password = it },
            label = { Text("API Key") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Text(
            text = "Your API key can be found at https://toolkit.solcast.com.au by clicking the menu at the top right of the page.",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSecondary,
        )
    }
}

@Preview
@Composable
fun SolcastSettingsViewPreview() {
    SolcastSettingsView()
}