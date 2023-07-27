package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SettingsFooterView(
    config: ConfigManaging, userManager: UserManaging, onLogout: () -> Unit, onRateApp: () -> Unit,
    onSendUsEmail: () -> Unit, onBuyMeCoffee: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsColumnWithChild(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                userManager.getUsername()?.let {
                    Text(
                        modifier = Modifier.padding(bottom = 24.dp),
                        text = stringResource(R.string.you_are_logged_in_as, it)
                    )
                }

                SettingsButton(
                    title = stringResource(R.string.logout),
                    onClick = onLogout,
                )
            }
        }

        Row {
            Button(
                onClick = onSendUsEmail,
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colors.primary,
                    backgroundColor = Color.Transparent
                ),
                elevation = null
            ) {
                Icon(
                    Icons.Default.Email, contentDescription = "Email", modifier = Modifier.padding(end = 5.dp)
                )
                Text(
                    stringResource(R.string.get_in_touch),
                    fontSize = 12.sp,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onRateApp,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colors.primary,
                    backgroundColor = Color.Transparent
                ),
                elevation = null
            ) {
                Icon(
                    Icons.Default.ThumbUp, contentDescription = "Thumbs Up", modifier = Modifier.padding(end = 5.dp)
                )
                Text(
                    text = stringResource(R.string.rate_this_app),
                    fontSize = 12.sp,
                )
            }

            Spacer(modifier = Modifier.widthIn(min = 20.dp))

            Button(
                onClick = onBuyMeCoffee,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colors.primary,
                    backgroundColor = Color.Transparent
                ),
                elevation = null
            ) {
                Icon(
                    Icons.Default.LocalCafe, contentDescription = "Coffee Cup", modifier = Modifier.padding(end = 5.dp)
                )
                Text(
                    text = stringResource(R.string.buy_me_a_coffee),
                    fontSize = 12.sp,
                )
            }
        }

        Text(
            "Version " + config.appVersion,
            modifier = Modifier.padding(top = 44.dp),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true, heightDp = 600, widthDp = 300)
@Composable
fun SettingsFooterViewPreview() {
    EnergyStatsTheme {
        SettingsFooterView(config = FakeConfigManager(),
            userManager = FakeUserManager(), onLogout = {}, onRateApp = {},
            onSendUsEmail = {}, onBuyMeCoffee = {})
    }
}