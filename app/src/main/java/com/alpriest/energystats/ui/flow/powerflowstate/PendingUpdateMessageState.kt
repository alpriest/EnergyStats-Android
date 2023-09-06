package com.alpriest.energystats.ui.flow.powerflowstate

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alpriest.energystats.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PendingUpdateMessageState(private val nextUpdateSeconds: Int, private val lastUpdate: LocalDateTime) : UpdateMessageState() {
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

    @Composable
    override fun lastUpdateMessage(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        return String.format(stringResource(R.string.last_update, lastUpdate.format(formatter)))
    }
}