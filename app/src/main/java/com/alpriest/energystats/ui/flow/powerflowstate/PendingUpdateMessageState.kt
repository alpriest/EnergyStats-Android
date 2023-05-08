package com.alpriest.energystats.ui.flow.powerflowstate

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alpriest.energystats.R

class PendingUpdateMessageState(private val nextUpdateSeconds: Int) : UpdateMessageState() {
    @Composable
    override fun updateMessage(): String {
        val next = when (nextUpdateSeconds) {
            in 0 until 60 -> "${nextUpdateSeconds}s"
            else -> {
                val minutes = nextUpdateSeconds / 60
                val remainder = nextUpdateSeconds % 60
                "${minutes}m ${remainder}s"
            }
        }

        return String.format(stringResource(R.string.nextUpdate, next))
    }
}