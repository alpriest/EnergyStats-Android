package com.alpriest.energystats.ui.login

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.ui.ClickableUrlText
import com.alpriest.energystats.ui.helpers.ButtonDefinition
import com.alpriest.energystats.ui.helpers.EqualWidthButtonList
import com.alpriest.energystats.ui.helpers.copyDebugData
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class APIKeyLoginViewModelFactory(
    private val userManager: UserManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return APIKeyLoginViewModel(userManager) as T
    }
}

class APIKeyLoginViewModel(
    private val userManager: UserManaging
) : ViewModel() {
    var errorMessageStream = MutableStateFlow<String?>(null)
    var apiKeyStream = MutableStateFlow<String>("")

    init {
        viewModelScope.launch {
            userManager.loggedInState.collect {
                if (it.loadState is LoggedOut) errorMessageStream.value = it.loadState.reason
            }
        }
    }

    suspend fun onLogin(apiKey: String, context: Context) {
        userManager.login(apiKey.trim(), context)
    }

    suspend fun onDemoLogin() {
        userManager.loginDemo()
    }
}

class APIKeyLoginView(private val userManager: UserManaging) {
    @Composable
    fun Content(
        viewModel: APIKeyLoginViewModel = viewModel(factory = APIKeyLoginViewModelFactory(userManager)),
        themeStream: MutableStateFlow<AppTheme>
    ) {
        val apiKey = viewModel.apiKeyStream.collectAsState().value
        val context = LocalContext.current
        val errorMessage = viewModel.errorMessageStream.collectAsState().value
        val scope = rememberCoroutineScope()
        val scrollState = rememberScrollState()
        var passwordVisible by rememberSaveable { mutableStateOf(false) }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
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
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.background),
                    onClick = { scope.launch { viewModel.onDemoLogin() } }
                ) {
                    Text(
                        stringResource(R.string.try_demo),
                        color = colorScheme.onSecondary
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Button(onClick = {
                    scope.launch { viewModel.onLogin(apiKey, context) }
                }) {
                    Text(
                        stringResource(R.string.log_me_in),
                        color = colorScheme.onPrimary
                    )
                }
            }

            HowToObtainAPIKeyView().Content(themeStream, Modifier.padding(horizontal = 16.dp))
        }
    }
}

class HowToObtainAPIKeyView {
    @Composable
    fun Content(themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
        val showing = remember { mutableStateOf(false) }

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.how_to_get_api_key),
                color = colorScheme.onSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BulletPoint(1, stringResource(R.string.api_key_step_1), themeStream)
            BulletPoint(2, stringResource(R.string.api_key_step_2), themeStream)
            BulletPoint(3, stringResource(R.string.api_key_step_3), themeStream)
            BulletPoint(4, stringResource(R.string.api_key_step_4), themeStream)
            BulletPoint(5, stringResource(R.string.api_key_step_5), themeStream)
            BulletPoint(6, stringResource(R.string.api_key_step_6), themeStream)
            BulletPoint(7, stringResource(R.string.api_key_step_7), themeStream)

            Button(
                onClick = { showing.value = !showing.value },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.background,
                    contentColor = colorScheme.onSecondary
                ),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.find_out_more_about_your_api_key))
            }

            AnimatedVisibility(
                visible = showing.value,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Paragraph(stringResource(R.string.what_is_api_key_1), themeStream = themeStream)
                    Paragraph(stringResource(R.string.what_is_api_key_2), themeStream = themeStream)
                    Paragraph(stringResource(R.string.what_is_api_key_3), themeStream = themeStream)
                    Paragraph(stringResource(R.string.what_is_api_key_4), themeStream = themeStream)
                    Paragraph(stringResource(R.string.what_is_api_key_5), themeStream = themeStream)
                }
            }
        }
    }

    @Composable
    fun Paragraph(text: String, themeStream: MutableStateFlow<AppTheme>) {
        ClickableUrlText(
            text,
            textStyle = TextStyle(colorScheme.onSecondary),
            themeStream = themeStream,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }

    @Composable
    fun BulletPoint(number: Int, text: String, themeStream: MutableStateFlow<AppTheme>) {
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
                themeStream = themeStream,
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
            themeStream = MutableStateFlow(AppTheme.demo())
        )
    }
}
