package com.example.myculina.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myculina.data.auth.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    // UI State
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    // User info
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        // Observe auth state changes
        viewModelScope.launch {
            authRepository.authStateFlow.collect { user ->
                _currentUser.value = user
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    // Sign in with Google
    fun signInWithGoogle() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val result = authRepository.signInWithGoogle()

                result.onSuccess { user ->
                    Log.d(TAG, "Sign-in successful: ${user.displayName}")
                    _authState.value = AuthState.Authenticated(user)
                }.onFailure { exception ->
                    Log.e(TAG, "Sign-in failed", exception)
                    _authState.value = AuthState.Error(exception.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in exception", e)
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Sign out
    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    // Check if user is signed in
    fun isUserSignedIn(): Boolean {
        return authRepository.isUserSignedIn()
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            authRepository.loginWithEmail(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Login failed")
                }
        }
    }

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            authRepository.registerWithEmail(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Registration failed")
                }
        }
    }


}

// Auth State sealed class
sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}