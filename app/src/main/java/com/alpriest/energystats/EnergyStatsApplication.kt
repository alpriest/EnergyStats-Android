package com.alpriest.energystats

import android.app.Application
import android.content.Context
import com.alpriest.energystats.ui.AppContainer

class EnergyStatsApplication : Application() {
    var context: Context? = null

    val appContainer by lazy {
        AppContainer(context!!)
    }

    override fun onCreate() {
        super.onCreate()

        context = applicationContext
    }
}
