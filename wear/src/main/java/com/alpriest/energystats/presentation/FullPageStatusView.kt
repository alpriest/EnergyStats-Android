package com.alpriest.energystats.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FullPageStatusView(
    iconScale: IconScale,
    icon: @Composable () -> Unit,
    line1: @Composable () -> Unit,
    line2: @Composable () -> Unit,
) {
    Column(
        modifier = if (iconScale == IconScale.LARGE) Modifier.fillMaxWidth().fillMaxHeight() else Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                modifier = Modifier.height(iconScale.frameHeight())
            ) {
                icon()
            }

            line1()
            line2()
        }
    }
}