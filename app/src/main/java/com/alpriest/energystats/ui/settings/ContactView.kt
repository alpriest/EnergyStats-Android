package com.alpriest.energystats.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun ContactView(navController: NavHostController, config: ConfigManaging) {
    val uriHandler = LocalUriHandler.current

    SettingsPage {
        SettingsColumn(
            header = stringResource(R.string.contact_header_techsupport)
        ) {
            InlineSettingsNavButton(
                title = stringResource(R.string.foxess_community),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://www.foxesscommunity.com/") }
            )
            HorizontalDivider()

            InlineSettingsNavButton(
                title = stringResource(R.string.facebook_group),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://www.facebook.com/groups/foxessownersgroup") }
            )
        }

        SettingsColumn(
            header = stringResource(R.string.contact_header_faq)
        ) {
            InlineSettingsNavButton(
                title = stringResource(R.string.frequently_asked_questions),
                onClick = { navController.navigate(SettingsScreen.FAQ.name) }
            )
        }

        SettingsColumn(
            header = stringResource(R.string.contact_header_getintouch),
            footer = stringResource(R.string.contact_footer_getintouch)
        ) {
            InlineSettingsNavButton(
                title = stringResource(R.string.contact),
                onClick = { uriHandler.openUri("mailto:energystatsapp@gmail.com?subject=Android%20App%20${config.appVersion}") }
            )
        }
    }
}

@Preview(heightDp = 640)
@Composable
fun PreviewContactView() {
    val context = LocalContext.current
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        ContactView(NavHostController(context), FakeConfigManager())
    }
}