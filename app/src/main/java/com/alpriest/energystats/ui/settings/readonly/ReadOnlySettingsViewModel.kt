package com.alpriest.energystats.ui.settings.readonly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ReadOnlySettingsViewData(
    val passcode: String,
    val isReadOnly: Boolean
)

class ReadOnlySettingsViewModelFactory(
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReadOnlySettingsViewModel(configManager) as T
    }
}

class ReadOnlySettingsViewModel(
    private val configManager: ConfigManaging
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    private val _viewDataStream = MutableStateFlow(ReadOnlySettingsViewData("", configManager.isReadOnly))
    val viewDataStream: StateFlow<ReadOnlySettingsViewData> = _viewDataStream

    fun onPasscodeChanged(passcode: String) {
        val viewData = _viewDataStream.value
        if (passcode.length == 4) {
            when (viewData.isReadOnly) {
                true ->
                    if (passcode == configManager.readOnlyPasscode) {
                        configManager.isReadOnly = false
                        _viewDataStream.value = _viewDataStream.value.copy(
                            isReadOnly = false,
                            passcode = ""
                        )
                        configManager.readOnlyPasscode = ""
                    } else {
                        alertDialogMessage.value = MonitorAlertDialogData(null, "Passcode was incorrect. Try again.", {
                            _viewDataStream.value = _viewDataStream.value.copy(
                                passcode = ""
                            )
                        })
                    }

                false -> {
                    configManager.isReadOnly = true
                    configManager.readOnlyPasscode = passcode
                    _viewDataStream.value = _viewDataStream.value.copy(passcode = "", isReadOnly = true)
                }
            }
        } else {
            _viewDataStream.value = _viewDataStream.value.copy(passcode = passcode)
        }
    }
}