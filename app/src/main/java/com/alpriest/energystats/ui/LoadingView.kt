package com.alpriest.energystats.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny

@Composable
fun LoadingView(title: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "SunIconTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "SunIconRotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(12.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 8.dp)
                .padding(end = 12.dp)
        ) {
            SunIcon(
                size = 36.dp,
                color = Sunny,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .padding(end = 12.dp)
                    .rotate(rotation)
            )

            Text(title)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingViewPreview() {
    EnergyStatsTheme {
        LoadingView(title = "Loading...")
    }
}