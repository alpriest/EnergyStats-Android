package com.alpriest.energystats.stores

interface CredentialStore {
    fun store(apiKey: String)
    fun getApiKey(): String?
    fun logout()
    fun hasCredentials(): Boolean
}