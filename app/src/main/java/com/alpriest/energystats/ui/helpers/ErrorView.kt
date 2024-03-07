package com.alpriest.energystats.ui.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.services.InMemoryLoggingNetworkStore
import com.alpriest.energystats.services.InvalidTokenException
import com.alpriest.energystats.services.MissingDataException
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.launch

private fun detailedErrorMessage(cause: Exception?, message: String, context: Context): String {
    cause?.let {
        when (it) {
            is InvalidTokenException -> {
                context.getString(R.string.invalid_api_token_needs_logout)
            }
            else -> if (it.localizedMessage != message) return "$message\n\n${it.localizedMessage}" else message
        }
    }
    return message
}

@Composable
fun ErrorView(cause: Exception?, reason: String, onRetry: suspend () -> Unit, onLogout: () -> Unit) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    var detailedMessage = detailedErrorMessage(cause, reason, context)

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
            detailedMessage,
            textAlign = TextAlign.Center
        )

        EqualWidthButtonList(
            listOf(
                ButtonDefinition(stringResource(R.string.retry)) { scope.launch { onRetry() } },
                ButtonDefinition(stringResource(R.string.copy_debug_data)) {
                    scope.launch {
                        copyDebugData(context)
                    }
                },
                ButtonDefinition(stringResource(R.string.foxess_cloud_status)) { uriHandler.openUri("https://monitor.foxesscommunity.com/status/foxess") },
                ButtonDefinition(stringResource(R.string.logout)) { onLogout() }
            )
        )
    }
}

fun copyDebugData(context: Context) {
    var text = ""

    InMemoryLoggingNetworkStore.shared.latestRequest?.let { request ->
        text += request
    }

    InMemoryLoggingNetworkStore.shared.latestResponse?.let { response ->
        text += "\n\n$response"
    }

    InMemoryLoggingNetworkStore.shared.latestResponseText?.let {
        text += "\n\n$it"
    }

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
}

@Preview
@Composable
fun ErrorPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        ErrorView(
            cause = MissingDataException(),
            reason = "BEGIN_OBJECT was expected but got something else instead. Will try again because something else went wrong too.",
            onRetry = {},
            onLogout = {}
        )
    }
}
