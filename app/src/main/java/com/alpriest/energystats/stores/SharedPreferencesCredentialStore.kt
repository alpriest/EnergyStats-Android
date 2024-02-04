package com.alpriest.energystats.stores

import android.content.SharedPreferences

class SharedPreferencesCredentialStore(private val sharedPreferences: SharedPreferences) :
    CredentialStore {
    private val apiKey = "API_KEY"

    override fun store(apiKey: String) {
        val editor = sharedPreferences.edit()
        editor.putString(this.apiKey, apiKey)
        editor.apply()
    }

    override fun getApiKey(): String? {
        return sharedPreferences.getString(apiKey, null)
    }

    override fun logout() {
        val editor = sharedPreferences.edit()
        editor.remove(apiKey)
        editor.apply()
    }

    override fun hasCredentials(): Boolean {
        return sharedPreferences.getString("PASSWORD", null) != null
    }

    override fun hasApiKey(): Boolean {
        return getApiKey() != null
    }
}