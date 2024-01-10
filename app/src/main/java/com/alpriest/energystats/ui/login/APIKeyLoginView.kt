package com.alpriest.energystats.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.ClickableUrlText
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.makeUrlAnnotatedString
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.webLinkColor

@Composable
fun APIKeyLoginView(
    errorMessage: String?,
    configManager: ConfigManaging
) {
    var apiKey by rememberSaveable { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            stringResource(R.string.enter_your_foxess_cloud_details),
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key") },
            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val (icon, iconColor) = if (showApiKey) {
                    Pair(
                        Icons.Filled.Visibility,
                        MaterialTheme.colors.primary
                    )
                } else {
                    Pair(
                        Icons.Filled.VisibilityOff,
                        Color.Gray
                    )
                }

                IconButton(onClick = { showApiKey = !showApiKey }) {
                    Icon(
                        icon,
                        contentDescription = "Visibility",
                        tint = iconColor
                    )
                }
            },
        )

        errorMessage?.let {
            Text(
                it,
                Modifier
                    .padding(top = 24.dp)
                    .padding(horizontal = 24.dp),
                color = Color.Red
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 24.dp)
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background),
                onClick = {/* TODO */ }
            ) {
                Text(
                    stringResource(R.string.try_demo),
                    color = MaterialTheme.colors.onSecondary
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Button(onClick = {
                // TODO
            }) {
                Text(
                    stringResource(R.string.log_me_in),
                    color = MaterialTheme.colors.onPrimary
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {

            Text(
                "To get your API key:",
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ClickableUrlText(
                text = "1. Login at https://www.foxesscloud.com/",
                textStyle = TextStyle(MaterialTheme.colors.onSecondary),
                themeStream = configManager.themeStream
            )
            Text("2. Click the person icon top-right")
            Text("3. Click the User Profile menu option")
            Text("4. Click Generate API key")
            Text("5. Copy the API key (make a note of it securely)")
            Text("6. Paste the API key above")

            ClickableUrlText(
                text = "This change to API key was required by FoxESS in January 2024. The FoxESS site does not function well on mobile devices. Please do not contact Energy Stats with issues about the FoxESS website, only FoxESS will be able to assist you in any issues with their site. service.uk@fox-ess.com",
                modifier = Modifier.padding(top = 12.dp),
                textStyle = TextStyle(MaterialTheme.colors.onSecondary),
                themeStream = configManager.themeStream
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun APIKeyLoginViewPreview() {
    EnergyStatsTheme {
        APIKeyLoginView(errorMessage = null, configManager = FakeConfigManager())
    }
}