package com.alpriest.energystats.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.demo
import com.alpriest.energystats.shared.ui.Sunny
import com.alpriest.energystats.ui.helpers.ClickableUrlText
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class APIKeyLoginView(private val userManager: UserManaging) {
    @Composable
    fun Content(
        viewModel: APIKeyLoginViewModel = viewModel(factory = APIKeyLoginViewModelFactory(userManager)),
        appSettingsStream: StateFlow<AppSettings>
    ) {
        val apiKey = viewModel.apiKeyStream.collectAsState().value
        val errorMessage = viewModel.errorMessageStream.collectAsState().value
        val scope = rememberCoroutineScope()
        var passwordVisible by rememberSaveable { mutableStateOf(false) }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Text(
                stringResource(R.string.enter_your_foxess_cloud_details),
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { viewModel.apiKeyStream.value = it },
                label = { Text("API Key") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
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
                ESButton(
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceContainer),
                    onClick = { scope.launch { viewModel.onDemoLogin() } }
                ) {
                    Text(
                        stringResource(R.string.try_demo),
                        color = colorScheme.onSecondary
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                ESButton(onClick = {
                    scope.launch { viewModel.onLogin(apiKey) }
                }) {
                    Text(
                        stringResource(R.string.log_me_in),
                        color = colorScheme.onPrimary
                    )
                }
            }

            HowToObtainAPIKeyView().Content(appSettingsStream, Modifier.padding(horizontal = 16.dp))
        }
    }
}

class HowToObtainAPIKeyView {
    @Composable
    fun Content(appSettingsStream: StateFlow<AppSettings>, modifier: Modifier = Modifier) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.how_to_get_api_key),
                color = colorScheme.onSecondary,
                modifier = Modifier.padding(bottom = 8.dp),
                fontWeight = FontWeight.Bold
            )
            BulletPoint(1, stringResource(R.string.api_key_step_1), appSettingsStream)
            Text("** Do not use the V2 website yet.**")
            BulletPoint(2, stringResource(R.string.api_key_step_2), appSettingsStream)
            BulletPoint(3, stringResource(R.string.api_key_step_3), appSettingsStream)
            BulletPoint(4, stringResource(R.string.api_key_step_4), appSettingsStream)
            BulletPoint(5, stringResource(R.string.api_key_step_5), appSettingsStream)
            BulletPoint(6, stringResource(R.string.api_key_step_6), appSettingsStream)
            BulletPoint(7, stringResource(R.string.api_key_step_7), appSettingsStream)

            Text(
                stringResource(R.string.what_is_an_api_key),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 32.dp),
                fontWeight = FontWeight.Bold
            )

            Column {
                Paragraph(stringResource(R.string.what_is_api_key_1), appSettingsStream = appSettingsStream)
                Paragraph(stringResource(R.string.what_is_api_key_2), appSettingsStream = appSettingsStream)
                Paragraph(stringResource(R.string.what_is_api_key_3), appSettingsStream = appSettingsStream)
                Paragraph(stringResource(R.string.what_is_api_key_4), appSettingsStream = appSettingsStream)
                Paragraph(stringResource(R.string.what_is_api_key_5), appSettingsStream = appSettingsStream)
            }
        }
    }

    @Composable
    fun Paragraph(text: String, appSettingsStream: StateFlow<AppSettings>) {
        ClickableUrlText(
            text,
            textStyle = TextStyle(colorScheme.onSecondary),
            appSettingsStream = appSettingsStream,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }

    @Composable
    fun BulletPoint(number: Int, text: String, appSettingsStream: StateFlow<AppSettings>) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Circle with Text overlay
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Sunny, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            ClickableUrlText(
                text = text,
                textStyle = TextStyle(colorScheme.onSecondary),
                appSettingsStream = appSettingsStream,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun APIKeyLoginViewPreview() {
    EnergyStatsTheme {
        APIKeyLoginView(FakeUserManager()).Content(
            appSettingsStream = MutableStateFlow(AppSettings.demo())
        )
    }
}
