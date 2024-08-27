package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SettingsFooterRowView(imageContent: @Composable () -> Unit, text: String, onClick: () -> Unit) {
    Row {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                contentColor = colorScheme.primary,
                containerColor = Color.Transparent
            ),
            elevation = null
        ) {
            imageContent()
            Text(
                text,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
fun SettingsFooterRowView(image: ImageVector, imageDescription: String, text: String, onClick: () -> Unit) {
    SettingsFooterRowView(
        imageContent = { Icon(image, contentDescription = imageDescription, modifier = Modifier.padding(end = 5.dp)) },
        text = text,
        onClick = onClick
    )
}

@Composable
fun SettingsFooterView(
    config: ConfigManaging,
    onLogout: () -> Unit,
    onRateApp: () -> Unit,
    onBuyMeCoffee: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

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
                SettingsNavButton(
                    title = stringResource(R.string.logout),
                    disclosureIcon = null,
                    onClick = onLogout,
                )
            }
        }

        SettingsFooterRowView(Icons.Default.Email, "Get in touch", stringResource(R.string.get_in_touch)) {
            uriHandler.openUri("mailto:energystatsapp@gmail.com?subject=Android%20App%20${config.appVersion}")
        }
        SettingsFooterRowView(Icons.Default.ThumbUp, "Rate this app", stringResource(R.string.rate_this_app), onRateApp)
        SettingsFooterRowView(Icons.Default.LocalCafe, "Buy me a coffee", stringResource(R.string.buy_me_a_coffee), onBuyMeCoffee)
        SettingsFooterRowView({
            Image(
                painter = painterResource(id = R.drawable.paypal_logo),
                contentDescription = "Donate via PayPal",
                modifier = Modifier.padding(end = 6.dp)
            )
        }, "Donate via PayPal") { uriHandler.openUri("https://www.paypal.me/alpriest") }

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
            onLogout = {}, onRateApp = {}) {}
    }
}