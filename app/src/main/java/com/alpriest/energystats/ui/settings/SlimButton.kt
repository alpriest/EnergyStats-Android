package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.OutlinedESButton

@Composable
fun SlimButton(modifier: Modifier = Modifier.Companion, enabled: Boolean = true, onClick: () -> Unit, content: @Composable () -> Unit) {
    ESButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        content()
    }
}

@Composable
fun OutlinedSlimButton(modifier: Modifier = Modifier.Companion, onClick: () -> Unit, content: @Composable () -> Unit, colors: ButtonColors) {
    OutlinedESButton(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        content()
    }
}