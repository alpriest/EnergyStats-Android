package com.alpriest.energystats.helpers

import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import kotlinx.coroutines.flow.MutableStateFlow

interface AlertDialogMessageProviding {
    val alertDialogMessage: MutableStateFlow<MonitorAlertDialogData?>
    fun resetDialogMessage() {
        alertDialogMessage.value = null
    }
}