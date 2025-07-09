package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.theme.ESButton

@Composable
fun FindOutMoreView(uriHandler: UriHandler, url: String) {
    Column(
        modifier = Modifier.Companion.fillMaxWidth().padding(bottom = 8.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        ESButton(
            onClick = {
                uriHandler.openUri(url)
            },
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                containerColor = Color.Companion.Transparent
            ),
            elevation = null,
        ) {
            Icon(
                Icons.Default.OpenInBrowser, contentDescription = "Open In Browser", modifier = Modifier.Companion.padding(end = 5.dp)
            )
            Text(
                stringResource(R.string.find_out_more),
                fontSize = 12.sp,
            )
        }
    }
}