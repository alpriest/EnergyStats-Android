package com.alpriest.energystats.ui.settings

import com.alpriest.energystats.stores.CredentialStore

class FakeCredentialStore : CredentialStore {
    override fun store(apiKey: String) {

    }

    override fun getApiKey(): String? {
        return null
    }

    override fun logout() {

    }

    override fun hasApiKey(): Boolean {
        return false
    }

    override fun hasCredentials(): Boolean {
        return false
    }
}