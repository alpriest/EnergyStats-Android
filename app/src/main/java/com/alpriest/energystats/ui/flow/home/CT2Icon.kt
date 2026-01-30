package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.isDarkMode
import com.alpriest.energystats.shared.ui.iconBackgroundColor
import com.alpriest.energystats.shared.ui.iconForegroundColor
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CT2Icon(modifier: Modifier, appSettingsStream: StateFlow<AppSettings>) {
    val foregroundColor = iconForegroundColor(isDarkMode(appSettingsStream))
    val backgroundColor = iconBackgroundColor(isDarkMode(appSettingsStream))

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(5.dp))
    ) {
        Text(
            text = "CT2",
            modifier = Modifier.align(Alignment.Center),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = foregroundColor
        )
    }
}