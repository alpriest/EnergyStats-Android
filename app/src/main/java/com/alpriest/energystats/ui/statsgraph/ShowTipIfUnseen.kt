package com.alpriest.energystats.ui.statsgraph

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun ShowTipIfUnseen(type: TipType) {
    val context = LocalContext.current
    val tipKitManager = remember { TipKitManager() }

    LaunchedEffect(Unit) {
        tipKitManager.checkAndShow(type, context)
    }

    tipKitManager.activeTip.value?.let { tip ->
        TipDialog(tip, context) {
            tipKitManager.dismiss()
        }
    }
}