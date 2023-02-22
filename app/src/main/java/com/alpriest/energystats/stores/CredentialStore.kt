package com.alpriest.energystats.stores

interface CredentialStore {
    fun getUsername(): String?
    fun getHashedPassword(): String?
    fun store(username: String, hashedPassword: String)
    fun getToken(): String?
    fun setToken(token: String?)
    fun logout()
    fun hasCredentials(): Boolean
}