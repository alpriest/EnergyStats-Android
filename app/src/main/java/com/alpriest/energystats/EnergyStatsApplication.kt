package com.alpriest.energystats

import android.app.Application
import android.content.Context
import com.alpriest.energystats.ui.AppContainer

class EnergyStatsApplication : Application() {
    var context: Context? = null

    // Instance of AppContainer that will be used by all the Activities of the app
    val appContainer by lazy { AppContainer(context!!) }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}