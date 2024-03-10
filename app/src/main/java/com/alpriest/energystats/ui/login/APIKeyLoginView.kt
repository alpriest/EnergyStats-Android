package com.alpriest.energystats.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.ClickableUrlText
import com.alpriest.energystats.ui.helpers.ButtonDefinition
import com.alpriest.energystats.ui.helpers.EqualWidthButtonList
import com.alpriest.energystats.ui.helpers.copyDebugData
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.preview
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun APIKeyLoginView(
    errorMessage: String?,
    themeStream: MutableStateFlow<AppTheme>,
    onLogin: (String) -> Unit,
    onDemoLogin: () -> Unit
) {
    var apiKey by rememberSaveable { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    val context = LocalContext.current

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

            EqualWidthButtonList(listOf(
                ButtonDefinition(stringResource(R.string.copy_debug_data)) {
                    copyDebugData(context)
                }
            ))
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 24.dp)
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background),
                onClick = { onDemoLogin() }
            ) {
                Text(
                    stringResource(R.string.try_demo),
                    color = colors.onSecondary
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Button(onClick = {
                onLogin(apiKey)
            }) {
                Text(
                    stringResource(R.string.log_me_in),
                    color = MaterialTheme.colors.onPrimary
                )
            }
        }

        HowToObtainAPIKeyView(themeStream, Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
fun HowToObtainAPIKeyView(themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
    ) {

        Text(
            stringResource(R.string.how_to_get_api_key),
            modifier = Modifier.padding(bottom = 8.dp),
            color = colors.onSecondary
        )
        ClickableUrlText(
            text = stringResource(R.string.api_key_step_1),
            textStyle = TextStyle(colors.onSecondary),
            themeStream = themeStream
        )
        Text(stringResource(R.string.api_key_step_2), color = colors.onSecondary)
        Text(stringResource(R.string.api_key_step_3), color = colors.onSecondary)
        Text(stringResource(R.string.api_key_step_4), color = colors.onSecondary)
        Text(stringResource(R.string.api_key_step_5), color = colors.onSecondary)
        Text(stringResource(R.string.api_key_step_6), color = colors.onSecondary)
        Text(stringResource(R.string.api_key_step_7), color = colors.onSecondary)

        val text = stringResource(R.string.api_key_step_7)
        val key = stringResource(R.string.example_api_key)

        Text(
            buildAnnotatedString {
                append(text)

                withStyle(style = SpanStyle(color = Color.Red)) {
                    append(key)
                }
            },
            color = colors.onSecondary
        )

        ClickableUrlText(
            text = stringResource(R.string.api_change_reason),
            modifier = Modifier.padding(top = 12.dp),
            textStyle = TextStyle(colors.onSecondary),
            themeStream = themeStream
        )
    }
}

@Preview(showBackground = true)
@Composable
fun APIKeyLoginViewPreview() {
    EnergyStatsTheme {
        APIKeyLoginView(errorMessage = null, themeStream = MutableStateFlow(AppTheme.preview()),
            onDemoLogin = {}, onLogin = {})
    }
}
