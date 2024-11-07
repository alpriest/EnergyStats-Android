package com.alpriest.energystats.ui.flow

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.helpers.ButtonDefinition
import com.alpriest.energystats.ui.helpers.EqualWidthButtonList
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun BannerView(bannerAlertStream: MutableStateFlow<BannerAlertType?>) {
    val bannerAlertType = bannerAlertStream.collectAsState().value

    bannerAlertType?.let {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            when (it) {
                BannerAlertType.Offline -> OfflineDeviceBannerView {
                    bannerAlertStream.value = null
                }
            }
        }
    }
}

@Composable
private fun OfflineDeviceBannerView(onDismiss: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(12.dp)
            .background(colorScheme.background)
            .border(width = 2.dp, color = colorScheme.onSecondary)
            .padding(12.dp)
    ) {
        Text(
            stringResource(R.string.offline_device),
            style = typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = colorScheme.onSecondary
        )

        Text(
            stringResource(R.string.wifi_changed_offline_message),
            modifier = Modifier.padding(bottom = 8.dp),
            color = colorScheme.onSecondary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            EqualWidthButtonList(
                listOf(
                    ButtonDefinition(title = stringResource(R.string.watch), onClick = {
                        onDismiss()

                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://youtu.be/Gy8ASwQ984A")
                        }
                        context.startActivity(intent)
                    }),
                    ButtonDefinition(title = stringResource(R.string.dismiss), onClick = {
                        onDismiss()
                    })
                ),
                between = { Spacer(modifier = Modifier.width(24.dp)) }
            )
        }

        Text(
            stringResource(R.string.you_can_also_find_this_video_link_in_the_settings_faq_section),
            style = typography.bodySmall.copy(fontSize = 10.sp),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = colorScheme.onSecondary
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
fun OfflineDeviceBannerViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        BannerView(bannerAlertStream = MutableStateFlow(BannerAlertType.Offline))
    }
}