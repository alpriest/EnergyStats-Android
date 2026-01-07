package com.alpriest.energystats.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class APIKeyLoginViewModel(
    private val userManager: UserManaging
) : ViewModel() {
    var errorMessageStream = MutableStateFlow<String?>(null)
    var apiKeyStream = MutableStateFlow("")

    init {
        viewModelScope.launch {
            userManager.loggedInState.collect {
                if (it.loadState is LoggedOut) errorMessageStream.value = it.loadState.reason
            }
        }
    }

    suspend fun onLogin(apiKey: String) {
        userManager.login(apiKey.trim())
    }

    suspend fun onDemoLogin() {
        userManager.loginDemo()
    }
}

class APIKeyLoginViewModelFactory(
    private val userManager: UserManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return APIKeyLoginViewModel(userManager) as T
    }
}
