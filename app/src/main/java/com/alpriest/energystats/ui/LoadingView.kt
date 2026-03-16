package com.alpriest.energystats.ui

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.ui.SunIcon
import com.alpriest.energystats.shared.ui.Sunny
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.delay

@Composable
fun LoadingView(loadState: LoadState.Active) {
    val context = LocalContext.current
    LoadingView(stringResource(loadState.titleResId), stringResource(loadState.longOperationTitleResId))
}

@Composable
fun LoadingView(title: String, longOperationTitle: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "SunIconTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "SunIconRotation"
    )

    var currentTitle by remember { mutableStateOf(title) }

    LaunchedEffect(Unit) {
        delay(10_000)  // 10 seconds
        currentTitle = longOperationTitle
    }

    val shape = RoundedCornerShape(4.dp)


    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)

    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background, shape)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = shape
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .padding(end = 14.dp)
            ) {
                SunIcon(
                    size = 36.dp,
                    color = Sunny,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .padding(end = 16.dp)
                        .rotate(rotation)
                )

                Text(
                    currentTitle,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoadingViewPreview() {
    EnergyStatsTheme {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
            LoadingView(title = "Loading...", longOperationTitle = "Still loading...")
        }
    }
}