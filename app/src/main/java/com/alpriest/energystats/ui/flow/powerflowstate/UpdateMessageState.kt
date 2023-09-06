package com.alpriest.energystats.ui.flow.powerflowstate

import androidx.compose.runtime.Composable

sealed class UpdateMessageState {
    @Composable
    abstract fun updateMessage(): String

    @Composable
    abstract fun lastUpdateMessage(): String
}