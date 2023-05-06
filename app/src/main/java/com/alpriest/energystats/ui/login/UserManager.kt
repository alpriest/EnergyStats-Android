package com.alpriest.energystats.ui.login

import androidx.annotation.UiThread
import com.alpriest.energystats.services.BadCredentialsException
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginStateHolder(
    val loadState: LoginState
)

sealed class LoginState {}
class LoggedOut(val reason: String? = null) : LoginState() {}
object LoggingIn : LoginState() {}
object LoggedIn : LoginState() {}

interface UserManaging {
    val loggedInState: StateFlow<LoginStateHolder>

    @UiThread
    suspend fun login(
        username: String,
        password: String
    )

    fun logout()
    fun getUsername(): String?
    suspend fun loginDemo()
}

class UserManager(
    private var configManager: ConfigManaging,
    private val networking: Networking,
    private val store: CredentialStore
) : UserManaging {
    private val _loggedInState = MutableStateFlow(LoginStateHolder(LoggedOut()))
    override val loggedInState: StateFlow<LoginStateHolder> = _loggedInState.asStateFlow()

    init {
        if (store.hasCredentials()) {
            _loggedInState.value = LoginStateHolder(LoggedIn)
        }
    }

    override suspend fun loginDemo() {
        configManager.isDemoUser = true
        store.store("demo", "user")
        configManager.fetchDevices()
        _loggedInState.value = LoginStateHolder(LoggedIn)
    }

    override suspend fun login(
        username: String,
        password: String
    ) {
        if (username.isBlank() || password.isBlank()) {
            return
        }

        _loggedInState.value = LoginStateHolder(LoggingIn)

        try {
            val hashedPassword = Encryption.md5(password)
            networking.verifyCredentials(username, hashedPassword)
            store.store(username, hashedPassword)
            configManager.fetchDevices()
            configManager.fetchFirmwareVersions()
            configManager.fetchVariables()
            _loggedInState.value = LoginStateHolder(LoggedIn)
        } catch (e: BadCredentialsException) {
            logout()
            _loggedInState.value = LoginStateHolder(LoggedOut("Wrong credentials, try again"))
        } catch (e: Exception) {
            logout()
            _loggedInState.value = LoginStateHolder(LoggedOut("Could not login. Check your internet connection. ${e.localizedMessage}"))
        }
    }

    override fun logout() {
        store.logout()
        configManager.logout()
        _loggedInState.value = LoginStateHolder(LoggedOut())
    }

    override fun getUsername(): String? {
        return store.getUsername()
    }
}


