package com.alpriest.energystats.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Composable
fun FAQView() {
    var markdownText: String by rememberSaveable { mutableStateOf("") }

    val client = OkHttpClient()

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(null) {
        coroutineScope.launch {
            val remoteUrl = "https://raw.githubusercontent.com/wiki/alpriest/EnergyStats-Android/FAQ.md"
            val response = fetchMarkdownContent(client, remoteUrl)
            response?.let {
                markdownText = it
            }
        }
    }

    MarkdownText(markdown = markdownText)
}

suspend fun fetchMarkdownContent(client: OkHttpClient, remoteUrl: String): String? {
    return withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(remoteUrl)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        }
    }
}