package com.alpriest.energystats.tabs

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DemoOverlay() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Companion.Red),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.Companion
            .padding(2.dp)
            .offset(x = 16.dp, y = 2.dp)
    ) {
        Text(
            "Demo",
            color = Color.Companion.White,
            fontWeight = FontWeight.Companion.Bold,
            modifier = Modifier.Companion
                .padding(horizontal = 2.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}