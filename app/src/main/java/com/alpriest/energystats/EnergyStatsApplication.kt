package com.alpriest.energystats

import android.app.Application
import android.content.Context
import com.alpriest.energystats.ui.AppContainer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class EnergyStatsApplication : Application() {
    var context: Context? = null

    // Instance of AppContainer that will be used by all the Activities of the app
    val appContainer by lazy { AppContainer(context!!) }

    override fun onCreate() {
        super.onCreate()

        GlobalScope.launch { appContainer.configManager.fetchFirmwareVersions() }

        context = applicationContext
    }
}