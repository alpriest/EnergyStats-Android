package com.alpriest.energystats.ui.login

import android.content.Context
import androidx.annotation.UiThread
import com.alpriest.energystats.R
import com.alpriest.energystats.services.BadCredentialsException
import com.alpriest.energystats.services.InvalidTokenException
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.SocketTimeoutException

data class LoginStateHolder(
    val loadState: LoginState
)

sealed class LoginState
class LoggedOut(val reason: String? = null) : LoginState()
object LoggingIn : LoginState()
object LoggedIn : LoginState()

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
        }
    }

    override suspend fun loginDemo() {
        configManager.isDemoUser = true
        store.store("demo")
        configManager.themeStream.value = AppTheme.demo()
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

        try {
            store.store(apiKey)
            configManager.fetchDevices()
            try {
                configManager.fetchPowerStationDetail()
            } catch (_: Exception) {}
            _loggedInState.value = LoginStateHolder(LoggedIn)
        } catch (e: BadCredentialsException) {
            logout()
            if (apiKey.isValidApiKey) {
                _loggedInState.value = LoginStateHolder(LoggedOut(context.getString(R.string.wrong_credentials_try_again)))
            } else {
                _loggedInState.value = LoginStateHolder(LoggedOut(
                    context.getString(R.string.invalid_api_key_format) + "\n\n" +
                    context.getString(R.string.what_is_api_key_3)
                ))
            }
        } catch (e: InvalidTokenException) {
            logout()
            _loggedInState.value = LoginStateHolder(LoggedOut(context.getString(R.string.invalid_token_logout_not_required)))
        } catch (e: SocketTimeoutException) {
            logout()
            _loggedInState.value = LoginStateHolder(LoggedOut(context.getString(R.string.foxess_timeout)))
        } catch (e: Exception) {
            logout()
            _loggedInState.value = LoginStateHolder(LoggedOut("Could not login. ${e.localizedMessage}"))
        }
    }

    override fun logout(clearDisplaySettings: Boolean, clearDeviceSettings: Boolean) {
        store.logout()

        if (configManager.isDemoUser) {
            configManager.logout(clearDisplaySettings = true, clearDeviceSettings = true)
        } else {
            configManager.logout(clearDisplaySettings = clearDisplaySettings, clearDeviceSettings = clearDeviceSettings)
        }

        configManager.logout(clearDisplaySettings, clearDeviceSettings)
        _loggedInState.value = LoginStateHolder(LoggedOut())
    }
}

private val apiKeyRegex = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

val String.isValidApiKey: Boolean
    get() = apiKeyRegex.matches(this)
