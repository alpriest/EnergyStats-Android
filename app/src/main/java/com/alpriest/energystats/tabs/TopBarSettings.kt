package com.alpriest.energystats.tabs

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

data class TopBarSettings(
    val topBarVisible: Boolean,
    val backButtonVisible: Boolean,
    val title: String,
    val actions: @Composable RowScope.() -> Unit
)