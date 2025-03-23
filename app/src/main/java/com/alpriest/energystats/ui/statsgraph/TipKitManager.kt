package com.alpriest.energystats.ui.statsgraph

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.alpriest.energystats.R

enum class TipType(val key: String) {
    statsGraphDecimalPlacesFixedTo1(
        "statsGraphDecimalPlacesFixedTo1"
    );

    fun title(context: Context): String {
        return when (this) {
            statsGraphDecimalPlacesFixedTo1 -> context.getString(R.string.tipkit_statsGraphDecimalPlacesFixedTo1_title)
        }
    }

    fun body(context: Context): String {
        return when (this) {
            statsGraphDecimalPlacesFixedTo1 -> context.getString(R.string.tipkit_statsGraphDecimalPlacesFixedTo1_body)
        }
    }

    val sharedPreferenceKey: String
        get() {
            return "tipkit_$key"
        }
}

class TipKitManager {
    private val _activeTip = mutableStateOf<TipType?>(null)
    val activeTip: State<TipType?> = _activeTip

    fun checkAndShow(tip: TipType, context: Context) {
        if (!hasSeen(tip, context)) {
            _activeTip.value = tip
            markAsSeen(tip, context)
        }
    }

    fun dismiss() {
        _activeTip.value = null
    }

    private fun hasSeen(tip: TipType, context: Context): Boolean {
        val prefs = context.getSharedPreferences(tip.sharedPreferenceKey, Context.MODE_PRIVATE)
        return prefs.getBoolean(tip.sharedPreferenceKey, false)
    }

    private fun markAsSeen(tip: TipType, context: Context) {
        val prefs = context.getSharedPreferences(tip.sharedPreferenceKey, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(tip.sharedPreferenceKey, true).apply()
    }
}

@Composable
fun TipDialog(tip: TipType, context: Context, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tip.title(context)) },
        text = { Text(tip.body(context)) },
        confirmButton = {
            Text(context.getString(R.string.ok), modifier = Modifier.clickable(onClick = onDismiss))
        }
    )
}
