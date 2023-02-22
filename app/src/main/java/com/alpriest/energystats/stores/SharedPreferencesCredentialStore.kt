package com.alpriest.energystats.stores

import android.content.SharedPreferences

class SharedPreferencesCredentialStore(private val sharedPreferences: SharedPreferences) :
    CredentialStore {
    private val TOKEN = "TOKEN"
    private val USERNAME = "USERNAME"
    private val PASSWORD = "PASSWORD"

    override fun getUsername(): String? {
        return sharedPreferences.getString(USERNAME, null)
    }

    override fun getHashedPassword(): String? {
        return sharedPreferences.getString(PASSWORD, null)
    }

    override fun store(username: String, hashedPassword: String) {
        val editor = sharedPreferences.edit()
        editor.putString(USERNAME, username)
        editor.putString(PASSWORD, hashedPassword)
        editor.apply()
    }

    override fun getToken(): String? {
        return sharedPreferences.getString(TOKEN, null)
    }

    override fun setToken(token: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(TOKEN, token)
        editor.apply()
    }

    override fun logout() {
        val editor = sharedPreferences.edit()
        editor.remove(TOKEN)
        editor.remove(USERNAME)
        editor.remove(PASSWORD)
        editor.apply()
    }

    override fun hasCredentials(): Boolean {
        return getUsername() != null && getHashedPassword() != null
    }
}