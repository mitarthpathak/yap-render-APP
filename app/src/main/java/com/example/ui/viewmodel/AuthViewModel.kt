package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.local.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Where the user currently sits in the pre-app flow. */
enum class AuthGate {
    LOADING,
    UNAUTHENTICATED,
    ONBOARDING,
    AUTHENTICATED
}

data class AuthUiState(
    val gate: AuthGate = AuthGate.LOADING,
    val account: AuthRepository.Account? = null,
    val stats: AuthRepository.Stats = AuthRepository.Stats(),
    val history: List<AuthRepository.HistoryEntry> = emptyList(),
    val isSubmitting: Boolean = false,
    val error: String? = null,
    /** Bumped after a fresh sign-in so the UI can fire the welcome animation once. */
    val welcomeNonce: Long = 0L,
    val welcomeName: String = ""
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AuthRepository.get(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentAccount: AuthRepository.Account? get() = _uiState.value.account

    init {
        resolveGate(initial = true)
    }

    private fun resolveGate(initial: Boolean = false) {
        val account = repo.currentAccount()
        val gate = when {
            account == null -> AuthGate.UNAUTHENTICATED
            !repo.hasSeenOnboarding() -> AuthGate.ONBOARDING
            else -> AuthGate.AUTHENTICATED
        }
        _uiState.update {
            it.copy(
                gate = gate,
                account = account,
                stats = repo.stats(),
                history = repo.history(),
                error = null,
                isSubmitting = false
            )
        }
        // On a cold start into an existing session, still greet the user.
        if (initial && account != null && gate == AuthGate.AUTHENTICATED) {
            fireWelcome(account.name)
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun signIn(email: String, password: String) = submit {
        repo.signIn(email, password)
    }

    fun signUp(
        name: String,
        email: String,
        password: String,
        role: String,
        organization: String,
        goal: String
    ) = submit {
        repo.signUp(name, email, password, role, organization, goal)
    }

    fun continueAsGuest() {
        repo.continueAsGuest()
        val account = repo.currentAccount()
        _uiState.update {
            it.copy(
                gate = if (repo.hasSeenOnboarding()) AuthGate.AUTHENTICATED else AuthGate.ONBOARDING,
                account = account,
                stats = repo.stats(),
                history = repo.history(),
                error = null
            )
        }
        account?.let { fireWelcome(it.name) }
    }

    private inline fun submit(block: () -> AuthRepository.AuthResult) {
        _uiState.update { it.copy(isSubmitting = true, error = null) }
        when (val result = block()) {
            is AuthRepository.AuthResult.Success -> {
                val account = repo.currentAccount()
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = null,
                        account = account,
                        stats = repo.stats(),
                        history = repo.history(),
                        gate = if (repo.hasSeenOnboarding()) AuthGate.AUTHENTICATED else AuthGate.ONBOARDING
                    )
                }
                account?.let { fireWelcome(it.name) }
            }
            is AuthRepository.AuthResult.Failure -> {
                _uiState.update { it.copy(isSubmitting = false, error = result.message) }
            }
        }
    }

    fun finishOnboarding() {
        repo.setOnboardingSeen()
        _uiState.update { it.copy(gate = AuthGate.AUTHENTICATED) }
    }

    fun signOut() {
        repo.signOut()
        _uiState.update {
            AuthUiState(gate = AuthGate.UNAUTHENTICATED)
        }
    }

    fun updateProfile(name: String, role: String, organization: String, goal: String): Boolean {
        val result = repo.updateProfile(name, role, organization, goal)
        return when (result) {
            is AuthRepository.AuthResult.Success -> {
                _uiState.update { it.copy(account = repo.currentAccount(), error = null) }
                true
            }
            is AuthRepository.AuthResult.Failure -> {
                _uiState.update { it.copy(error = result.message) }
                false
            }
        }
    }

    fun savePreferences(avatarType: String, speed: Float) {
        repo.savePreferences(avatarType, speed)
        _uiState.update { it.copy(account = repo.currentAccount()) }
    }

    /** Hooked into the translator: called every time a phrase is played. */
    fun recordTranslation(phrase: String) {
        repo.recordTranslation(phrase)
        _uiState.update { it.copy(stats = repo.stats(), history = repo.history()) }
    }

    fun clearHistory() {
        repo.clearHistory()
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun consumeWelcome() {
        _uiState.update { it.copy(welcomeNonce = 0L) }
    }

    private fun fireWelcome(name: String) {
        _uiState.update {
            it.copy(welcomeNonce = System.currentTimeMillis(), welcomeName = name)
        }
    }
}
