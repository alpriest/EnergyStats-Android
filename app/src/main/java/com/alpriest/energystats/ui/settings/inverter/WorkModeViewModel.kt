package com.alpriest.energystats.ui.settings.inverter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging

class WorkModeViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Networking::class.java, ConfigManaging::class.java)
            .newInstance(network, configManager)
    }
}

class WorkModeViewModel(
    val network: Networking,
    val configManager: ConfigManaging
) : ViewModel() {
    init {
        println("AWP WorkModeViewModel")
    }
}