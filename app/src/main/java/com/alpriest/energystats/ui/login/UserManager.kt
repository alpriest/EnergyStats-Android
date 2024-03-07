package com.alpriest.energystats.ui.login

import android.content.Context
import androidx.annotation.UiThread
import com.alpriest.energystats.R
import com.alpriest.energystats.services.BadCredentialsException
import com.alpriest.energystats.services.InvalidTokenException
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginStateHolder(
    val loadState: LoginState
)

sealed class LoginState
class LoggedOut(val reason: String? = null) : LoginState()
object LoggingIn : LoginState()
object LoggedIn : LoginState()
object RequiresUpgrade : LoginState()

interface UserManaging {
    val loggedInState: StateFlow<LoginStateHolder>
    val store: CredentialStore

    fun logout(clearDisplaySettings: Boolean = true, clearDeviceSettings: Boolean = true)
    suspend fun loginDemo()
    @UiThread
    suspend fun login(apiKey: String, context: Context)
}

class UserManager(
    private var configManager: ConfigManaging,
    override val store: CredentialStore
) : UserManaging {
    private val _loggedInState = MutableStateFlow(LoginStateHolder(LoggedOut()))
    override val loggedInState: StateFlow<LoginStateHolder> = _loggedInState.asStateFlow()

    init {
        if (store.hasApiKey()) {
            _loggedInState.value = LoginStateHolder(LoggedIn)
        } else if (store.hasCredentials()) {
            _loggedInState.value = LoginStateHolder(RequiresUpgrade)
        }
    }

    override suspend fun loginDemo() {
        configManager.isDemoUser = true
        store.store("demo")
        configManager.fetchDevices()
        _loggedInState.value = LoginStateHolder(LoggedIn)
    }

    override suspend fun login(
        apiKey: String,
        context: Context
    ) {
        if (apiKey.isBlank()) {
            return
        }

        _loggedInState.value = LoginStateHolder(LoggingIn)
        var currentAction = "fetch login Token"

        try {
            store.store(apiKey)
            currentAction = "fetch devices"
            configManager.fetchDevices()
            _loggedInState.value = LoginStateHolder(LoggedIn)
        } catch (e: BadCredentialsException) {
            logout()
            _loggedInState.value = LoginStateHolder(LoggedOut(context.getString(R.string.wrong_credentials_try_again)))
        } catch (e: InvalidTokenException) {
            logout()
            _loggedInState.value = LoginStateHolder(LoggedOut(context.getString(R.string.invalid_token_logout_not_required)))
        } catch (e: Exception) {
            logout()
            _loggedInState.value = LoginStateHolder(LoggedOut("Could not login (${currentAction}). ${e.localizedMessage}"))
        }
    }

    override fun logout(clearDisplaySettings: Boolean, clearDeviceSettings: Boolean) {
        store.logout()
        configManager.logout(clearDisplaySettings, clearDeviceSettings)
        _loggedInState.value = LoginStateHolder(LoggedOut())
    }
}


