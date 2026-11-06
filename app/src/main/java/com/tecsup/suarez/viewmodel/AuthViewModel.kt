package com.tecsup.suarez.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.tecsup.suarez.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val user: FirebaseUser? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    // Si ya hay sesión guardada, entra directo a la app
    private val _state = MutableStateFlow(AuthUiState(user = repo.currentUser))
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (!validate(email, password)) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            repo.login(email, password)
                .onSuccess { _state.value = AuthUiState(user = it) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = it.message) }
        }
    }

    fun register(email: String, password: String, confirm: String) {
        if (!validate(email, password)) return
        if (password != confirm) {
            _state.value = _state.value.copy(error = "Las contraseñas no coinciden")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            repo.register(email, password)
                .onSuccess { _state.value = AuthUiState(user = it) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = it.message) }
        }
    }

    fun logout() {
        repo.logout()
        _state.value = AuthUiState()
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    private fun validate(email: String, password: String): Boolean {
        val msg = when {
            email.isBlank() || password.isBlank() -> "Completa todos los campos"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Correo inválido"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
        if (msg != null) _state.value = _state.value.copy(error = msg)
        return msg == null
    }
}
