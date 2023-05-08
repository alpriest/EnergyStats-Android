package com.alpriest.energystats.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import com.alpriest.energystats.BuildConfig
import com.alpriest.energystats.models.RawDataStore
import com.alpriest.energystats.models.RawDataStoring
import com.alpriest.energystats.services.NetworkFacade
import com.alpriest.energystats.services.NetworkService
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.stores.SharedPreferencesConfigStore
import com.alpriest.energystats.stores.SharedPreferencesCredentialStore
import com.alpriest.energystats.ui.login.*

class AppContainer(private val context: Context) {
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
            rawDataStore = rawDataStore,
            appVersion = BuildConfig.VERSION_NAME
        )
    }

    val userManager: UserManaging by lazy {
        UserManager(configManager, networking, credentialStore)
    }

    fun openAppInPlayStore() {
        val uri = Uri.parse("market://details?id=" + context.packageName)
        val goToMarketIntent = Intent(Intent.ACTION_VIEW, uri)

        val flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or FLAG_ACTIVITY_NEW_TASK
        goToMarketIntent.addFlags(flags)

        try {
            startActivity(context, goToMarketIntent, null)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)
            )

            startActivity(context, intent, null)
        }
    }

    fun sendUsEmail() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        val subject = "Android App"
        val data = Uri.parse("mailto:energystatsapp@gmail.com?subject=" + Uri.encode(subject))
        intent.data = data
        startActivity(context, intent, null)
    }
}