package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Stack

data class EditScheduleData(val schedule: Schedule, val allowDeletion: Boolean)

class EditScheduleStore {
    private val stack = SafeStack<EditScheduleData>()

    fun push(data: EditScheduleData) {
        stack.push(data)
    }

    fun pop(): EditScheduleData? {
        return stack.pop()
    }

    companion object {
        val shared: EditScheduleStore = EditScheduleStore()
    }
}

class EditScheduleViewModelFactory(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging,
    private val navHostController: NavHostController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditScheduleViewModel(network, configManager, navHostController) as T
    }
}

class EditScheduleViewModel(
    val network: FoxESSNetworking,
    val config: ConfigManaging,
    val navHostController: NavHostController
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<String?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val scheduleStream = MutableStateFlow<Schedule?>(null)
    val allowDeletionStream = MutableStateFlow(false)

    fun load(context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        val data = EditScheduleStore.shared.pop() ?: return

        scheduleStream.value = data.schedule
        allowDeletionStream.value = data.allowDeletion
    }

    fun saveSchedule(context: Context) {
        viewModelScope.launch {
            navHostController.popBackStack()
        }
    }

    fun addTimePeriod() {
        TODO("Not yet implemented")
    }

    fun autoFillScheduleGaps() {
        TODO("Not yet implemented")
    }
}

class SafeStack<T> {
    private val items = mutableListOf<T>()

    val isEmpty: Boolean
        get() = items.isEmpty()

    fun push(item: T) {
        items.add(item)
    }

    fun pop(): T? {
        if (isEmpty) return null
        return items.removeAt(items.size - 1)
    }

    val size: Int
        get() = items.size
}