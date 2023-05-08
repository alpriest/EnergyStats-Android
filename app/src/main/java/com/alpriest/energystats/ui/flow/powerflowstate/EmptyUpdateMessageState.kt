package com.alpriest.energystats.ui.flow.powerflowstate

import androidx.compose.runtime.Composable

object EmptyUpdateMessageState : UpdateMessageState() {
    @Composable
    override fun updateMessage(): String {
        return " "
    }
}