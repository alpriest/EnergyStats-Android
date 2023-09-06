package com.alpriest.energystats.ui.flow.powerflowstate

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alpriest.energystats.R

object LoadingNowUpdateMessageState : UpdateMessageState() {
    @Composable
    override fun updateMessage(): String {
        return stringResource(R.string.loading)
    }

    @Composable
    override fun lastUpdateMessage(): String {
        return ""
    }
}