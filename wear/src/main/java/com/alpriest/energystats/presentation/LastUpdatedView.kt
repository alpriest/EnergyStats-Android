package com.alpriest.energystats.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.shared.helpers.fullDateTime
import com.google.android.horologist.compose.layout.fillMaxRectangle
import java.time.LocalDateTime

@Composable
fun LastUpdatedView(lastUpdated: LocalDateTime?) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxRectangle().verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Last updated")
        lastUpdated?.let {
            Text(it.fullDateTime(), textAlign = TextAlign.Center)
        } ?: Text("Never updated")
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true, fontScale = 1f)
@Composable
fun LastUpdatedPreviewSmallRound() {
    LastUpdatedView(LocalDateTime.now())
}
