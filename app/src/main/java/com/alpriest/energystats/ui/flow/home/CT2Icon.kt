package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.ui.flow.battery.iconBackgroundColor
import com.alpriest.energystats.ui.flow.battery.iconForegroundColor

@Composable
fun CT2Icon(modifier: Modifier) {
    val foregroundColor = iconForegroundColor()
    val backgroundColor = iconBackgroundColor()

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