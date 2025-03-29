package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun InvertView(modifier: Modifier) {
    val uriHandler = LocalUriHandler.current
    trackScreenView("Invert", "InvertView")

    SettingsPage(modifier) {
        SettingsColumn(
            modifier = Modifier.padding(16.dp),
            padding = PaddingValues(16.dp)
        ) {
            Text(stringResource(R.string.invert_energy_disclaimer))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ESButton(onClick = { uriHandler.openUri("https://invert.energy/fox-ess") }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.visit_invert),
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null
                            )
                        }
                        Text(
                            "https://invert.energy/fox-ess", fontWeight = FontWeight.Light
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 940)
@Composable
fun InvertViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        InvertView(Modifier)
    }
}