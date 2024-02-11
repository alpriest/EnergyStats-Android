package com.alpriest.energystats

import android.app.Application
import android.content.Context
import com.alpriest.energystats.ui.AppContainer

class EnergyStatsApplication : Application() {
    var context: Context? = null

    val appContainer by lazy {
        AppContainer(context!!)
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        context = applicationContext
    }

    companion object {
        private var instance: EnergyStatsApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}
