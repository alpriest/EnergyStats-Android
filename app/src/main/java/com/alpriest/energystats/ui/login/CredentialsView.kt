package com.alpriest.energystats.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CredentialsView(
    errorMessage: String?,
    onLogin: (String, String) -> Unit,
    onDemoLogin: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

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
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(R.string.username)) }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val (icon, iconColor) = if (showPassword) {
                    Pair(
                        Icons.Filled.Visibility,
                        colors.primary
                    )
                } else {
                    Pair(
                        Icons.Filled.VisibilityOff,
                        Color.Gray
                    )
                }

                IconButton(onClick = { showPassword = !showPassword }) {
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
                .padding(top = 24.dp)
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = colors.background),
                onClick = onDemoLogin
            ) {
                Text(
                    stringResource(R.string.try_demo),
                    color = colors.onSecondary
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Button(onClick = {
                onLogin(username, password)
            }) {
                Text(
                    stringResource(R.string.log_me_in),
                    color = colors.onPrimary
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CredentialsViewPreview() {
    EnergyStatsTheme {
        CredentialsView(
            errorMessage = "You got something wrong",
            onLogin = { _, _ -> },
            onDemoLogin = {}
        )
    }
}
