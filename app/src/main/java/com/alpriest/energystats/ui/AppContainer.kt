package com.alpriest.energystats.ui

import android.content.Context
import android.content.SharedPreferences
import com.alpriest.energystats.models.RawDataStore
import com.alpriest.energystats.models.RawDataStoring
import com.alpriest.energystats.services.NetworkFacade
import com.alpriest.energystats.stores.SharedPreferencesConfigStore
import com.alpriest.energystats.services.NetworkService
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.stores.SharedPreferencesCredentialStore
import com.alpriest.energystats.ui.login.*

class AppContainer(context: Context) {
    val rawDataStore: RawDataStoring = RawDataStore()
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(
            "com.alpriest.energystats",
            Context.MODE_PRIVATE
        )
    private val credentialStore: CredentialStore =
        SharedPreferencesCredentialStore(sharedPreferences)
    private val config = SharedPreferencesConfigStore(sharedPreferences)

    val networking: Networking by lazy {
        NetworkFacade(
            network = NetworkService(credentialStore, config),
            config = config
        )
    }

    val configManager: ConfigManaging by lazy {
        ConfigManager(
            config = config,
            networking = networking,
            rawDataStore = rawDataStore
        )
    }

    val userManager: UserManaging by lazy {
        UserManager(configManager, networking, credentialStore)
    }
}