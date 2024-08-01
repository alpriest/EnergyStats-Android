package com.alpriest.energystats.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SlowServerMessageView(dismiss: () -> Unit) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .shadow(6.dp)
                .background(MaterialTheme.colors.secondary)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.slow_performance),
                color = MaterialTheme.colors.onSecondary,
                style = MaterialTheme.typography.h2
            )

            Text(
                stringResource(R.string.slow_performance_message),
                color = MaterialTheme.colors.onSecondary
            )

            Image(
                painter = painterResource(R.drawable.slow_performance),
                contentDescription = "Slow performance",
            )

            Button(onClick = { dismiss() }) {
                Text(stringResource(id = R.string.ok))
            }
        }
    }
}

@Composable
fun SlowServerBannerView(onToggle: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Red)
            .clickable { onToggle() }
    ) {
        Text(
            stringResource(R.string.always_loading_tap_for_details),
            color = Color.White
        )
    }
}

@Preview(heightDp = 200)
@Composable
fun SlowServerMessagePreview() {
    EnergyStatsTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            SlowServerMessageView { }
        }
    }
}