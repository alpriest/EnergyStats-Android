package com.alpriest.energystats.preview

import com.alpriest.energystats.ui.login.LoggedIn
import com.alpriest.energystats.ui.login.LoginStateHolder
import com.alpriest.energystats.ui.login.UserManaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeUserManager : UserManaging {
    override val loggedInState: StateFlow<LoginStateHolder>
        get() = MutableStateFlow(LoginStateHolder(LoggedIn))

    override suspend fun login(username: String, password: String) {
    }

    override fun logout() {
    }

    override fun getUsername(): String {
        return "Bob"
    }

    override suspend fun loginDemo() {
    }
}
