package com.alpriest.energystats.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.BottomButtonConfiguration
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.SettingsPage
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun UpgradeRequiredView(userManager: UserManaging) {
    ContentWithBottomButtons(
        content = { modifier ->
            SettingsPage(modifier) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "IMPORTANT",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 22.dp)
                    )

                    Text(
                        "We've updated the login process for Energy Stats",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("What's changed?", fontSize = 20.sp)

                    Text("In December 2023, FoxESS asked all 3rd party integrations to migrate access to a new set of services to access customer data. These new services are known by FoxESS as their OpenAPI services. Energy Stats has been changed internally to use these new services (2,500 lines of code changed).")

                    Text("These new services are different from those used by the Fox apps and have some notable differences.")

                    MarkdownText("- **Rate limited to 1,440 requests per inverter per day**. Unless you leave Energy Stats on 24 hours a day this is unlikely to affect you.")

                    MarkdownText("- **Schedule templates are no longer available** as FoxESS have not yet made these available via their OpenAPI. When schedule templates become available support will be added.")

                    MarkdownText("- **Work mode is not configurable**. You will need to use a schedule to change your default inverter work mode.")

                    MarkdownText("- **Login is via an API key**. Instructions for finding your API key are on the login page.")

                    MarkdownText("- **Network calls are throttled to 1 per second**. You may notice some screens load less quickly than before.")
                }
            }
        }, buttons = listOf(
            BottomButtonConfiguration("Continue", { userManager.logout() })
        )
    )
}

@Preview(showBackground = true, heightDp = 440, widthDp = 500)
@Composable
fun DataSettingsViewPreview() {
    UpgradeRequiredView(FakeUserManager())
}