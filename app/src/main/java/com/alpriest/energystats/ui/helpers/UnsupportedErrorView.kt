package com.alpriest.energystats.ui.helpers

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.fetchMarkdownContent
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

@Composable
fun UnsupportedErrorView(onDismiss: () -> Unit) {
    var markdownText: String by rememberSaveable { mutableStateOf("") }

    val client = OkHttpClient()

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(null) {
        coroutineScope.launch {
            val remoteUrl = "https://raw.githubusercontent.com/wiki/alpriest/EnergyStats-Android/Unsupported.md"
            val response = fetchMarkdownContent(client, remoteUrl)
            response?.let {
                markdownText = it
            }
        }
    }

    val context = LocalContext.current
    val windowManager = remember { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    val metrics = DisplayMetrics().apply {
        windowManager.defaultDisplay.getRealMetrics(this)
    }
    val (width, height) = with(LocalDensity.current) {
        Pair(metrics.widthPixels.toDp() * 0.8f, metrics.heightPixels.toDp() * 0.8f)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.requiredSize(width, height)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unsupported",
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )

                MarkdownText(markdown = markdownText)
            }
        }
    }
}

@Preview
@Composable
fun UnsupportedErrorPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        UnsupportedErrorView({})
    }
}