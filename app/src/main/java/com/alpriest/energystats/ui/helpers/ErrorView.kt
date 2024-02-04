package com.alpriest.energystats.ui.helpers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.services.MissingDataException
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.launch

@Composable
fun ErrorView(ex: Exception?, reason: String, onRetry: suspend () -> Unit, onLogout: () -> Unit) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        Icon(
            Icons.Rounded.ErrorOutline,
            tint = Color.Red,
            contentDescription = "",
            modifier = Modifier
                .size(128.dp)
        )

        Text(
            text = stringResource(R.string.something_went_wrong_fetching_data_from_foxess_cloud),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp),
            fontSize = 16.sp
        )

        Text(
            reason,
            textAlign = TextAlign.Center
        )

        EqualWidthButtonList(
            listOf(
                ButtonDefinition(stringResource(R.string.retry)) { coroutineScope.launch { onRetry() } },
                ButtonDefinition(stringResource(R.string.foxess_cloud_status)) { uriHandler.openUri("https://monitor.foxesscommunity.com/status/foxess") },
                ButtonDefinition(stringResource(R.string.logout)) { onLogout() }
            )
        )
    }
}

@Preview
@Composable
fun ErrorPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        ErrorView(
            ex = MissingDataException(),
            reason = "BEGIN_OBJECT was expected but got something else instead. Will try again because something else went wrong too.",
            onRetry = {},
            onLogout = {}
        )
    }
}
