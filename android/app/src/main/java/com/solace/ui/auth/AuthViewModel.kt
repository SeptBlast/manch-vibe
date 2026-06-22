package com.solace.ui.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser
import com.solace.data.auth.AuthRepository
import com.solace.data.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Authenticated(val user: FirebaseUser) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

// TODO: replace with your Web client ID from the Firebase Console → Authentication → Sign-in providers → Google
private const val GOOGLE_WEB_CLIENT_ID = "414230295497-02h0ul369tn0hfck7vtnap3spl8aaopn.apps.googleusercontent.com"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Mirror Firebase auth state into uiState
        viewModelScope.launch {
            authRepo.currentUser.collect { user ->
                _uiState.value = if (user != null) AuthUiState.Authenticated(user)
                else AuthUiState.Idle
            }
        }
    }

    // ── Email ─────────────────────────────────────────────────────────────────

    fun signInWithEmail(email: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = authRepo.signInWithEmail(email, password)) {
                is AuthResult.Success -> AuthUiState.Authenticated(result.user)
                is AuthResult.Failure -> AuthUiState.Error(result.error.message ?: "Sign-in failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = authRepo.signUpWithEmail(email, password)) {
                is AuthResult.Success -> AuthUiState.Authenticated(result.user)
                is AuthResult.Failure -> AuthUiState.Error(result.error.message ?: "Sign-up failed")
            }
        }
    }

    // ── Google (Credential Manager) ───────────────────────────────────────────

    fun signInWithGoogle(activity: Activity) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            runCatching {
                val manager = CredentialManager.create(activity)
                val option = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()
                val response = manager.getCredential(activity, request)
                val credential = response.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                    authRepo.signInWithGoogle(googleCred.idToken)
                } else {
                    AuthResult.Failure(Exception("Unexpected credential type"))
                }
            }.getOrElse { AuthResult.Failure(it as Exception) }.let { result ->
                _uiState.value = when (result) {
                    is AuthResult.Success -> AuthUiState.Authenticated(result.user)
                    is AuthResult.Failure -> AuthUiState.Error(result.error.message ?: "Google sign-in failed")
                }
            }
        }
    }

    // ── Apple ─────────────────────────────────────────────────────────────────

    fun signInWithApple(activity: Activity) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = authRepo.signInWithApple(activity)) {
                is AuthResult.Success -> AuthUiState.Authenticated(result.user)
                is AuthResult.Failure -> AuthUiState.Error(result.error.message ?: "Apple sign-in failed")
            }
        }
    }

    // ── Sign out ──────────────────────────────────────────────────────────────

    fun signOut() {
        authRepo.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) _uiState.value = AuthUiState.Idle
    }
}
