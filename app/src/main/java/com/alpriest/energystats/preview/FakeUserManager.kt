package com.alpriest.energystats.preview

import com.alpriest.energystats.ui.login.LoggedIn
import com.alpriest.energystats.ui.login.LoginStateHolder
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.FakeCredentialStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeUserManager : UserManaging {
    override val store = FakeCredentialStore()

    override val loggedInState: StateFlow<LoginStateHolder>
        get() = MutableStateFlow(LoginStateHolder(LoggedIn))

    override suspend fun login(apiKey: String) {
    }

    override suspend fun logout(clearDisplaySettings: Boolean, clearDeviceSettings: Boolean) {
    }

    override suspend fun loginDemo() {
    }
}
