package com.alpriest.energystats.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.shared.helpers.fullDateTime
import java.time.LocalDateTime

@Composable
fun LastUpdatedView(lastUpdated: LocalDateTime?) {
    FullPageStatusView(
        IconScale.LARGE,
        icon = { },
        line1 = {
            Text("Last updated")

            lastUpdated?.let {
                Text(it.fullDateTime())
            } ?: Text("Never updated")
        },
        line2 = { }
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun LastUpdatedPreviewSmallRound() {
    LastUpdatedView(LocalDateTime.now())
}
