package com.alpriest.energystats.ui.login

import android.content.res.Configuration
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun WelcomeView(
    showingApiKey: Boolean,
    userManager: UserManaging,
    themeStream: MutableStateFlow<AppTheme>,
    onClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val maxHeight: Dp = if (isLandscape()) 200.dp else 800.dp

    Column(
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        WelcomeLogoView(
            showingApiKey,
            modifier = Modifier.heightIn(100.dp, maxHeight)
        )

        if (showingApiKey) {
            APIKeyLoginView(userManager = userManager).Content(themeStream = themeStream)
        } else {
            Text(
                stringResource(R.string.energy_management_at_your_fingertips),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 44.dp)
            )

            Button(
                onClick = onClick,
                modifier = Modifier
                    .padding(top = 44.dp)
                    .defaultMinSize(minWidth = 200.dp)
            ) {
                Text(stringResource(R.string.get_started))
            }
        }
    }
}

@Composable
fun WelcomeLogoView(showingApiKey: Boolean, modifier: Modifier = Modifier) {
    val painter = painterResource(id = R.drawable.es_icon)
    val animationSpec: AnimationSpec<Dp> = tween(durationMillis = 200, easing = EaseIn)
    val aspectRatio = painter.intrinsicSize.width / painter.intrinsicSize.height
    val animatedWidth: Dp by animateDpAsState(targetValue = if (showingApiKey) 160.dp else LocalConfiguration.current.screenWidthDp.dp, label = "logo width")
    val animatedHeight: Dp by animateDpAsState(targetValue = animatedWidth / aspectRatio, animationSpec = animationSpec, label = "logo height")
    val animatedPadding: Dp by animateDpAsState(targetValue = if (showingApiKey) 12.dp else 0.dp, animationSpec = animationSpec, label = "logo padding")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Yellow.copy(alpha = 0.2f))
            .padding(vertical = animatedPadding),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.es_icon),
            contentDescription = "Energy Stats logo",
            modifier = Modifier
                .height(animatedHeight)
                .width(animatedWidth)
                .fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Preview(showBackground = true, heightDp = 790, widthDp = 380)
@Composable
fun WelcomeViewPreview() {
    EnergyStatsTheme {
        WelcomeView(false, userManager = FakeUserManager(), MutableStateFlow(AppTheme.demo())) {}
    }
}
