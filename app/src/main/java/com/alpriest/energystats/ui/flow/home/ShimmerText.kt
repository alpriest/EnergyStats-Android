package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.valentinilk.shimmer.shimmer

@Composable
fun ShimmerText(
    shimmering: Boolean,
    text: String,
    color: Color = Color.Companion.Unspecified,
    fontSize: TextUnit = TextUnit.Companion.Unspecified,
    fontWeight: FontWeight? = null
) {
    Box(modifier = Modifier.Companion.let {
        if (shimmering) {
            it.shimmer()
        } else {
            it
        }
    }) {
        Text(
            text = text,
            color = if (shimmering) Color.Companion.Transparent else color,
            modifier = Modifier.Companion.background(if (shimmering) Color.Companion.LightGray else Color.Companion.Transparent),
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}