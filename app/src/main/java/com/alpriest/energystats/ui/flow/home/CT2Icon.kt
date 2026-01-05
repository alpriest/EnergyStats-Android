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
import com.alpriest.energystats.shared.ui.iconBackgroundColor
import com.alpriest.energystats.shared.ui.iconForegroundColor
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun CT2Icon(modifier: Modifier, themeStream: MutableStateFlow<AppTheme>) {
    val foregroundColor = iconForegroundColor(isDarkMode(themeStream))
    val backgroundColor = iconBackgroundColor(isDarkMode(themeStream))

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