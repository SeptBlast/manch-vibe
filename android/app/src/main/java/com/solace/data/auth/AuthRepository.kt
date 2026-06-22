package com.solace.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Failure(val error: Exception) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor() {

    private val auth: FirebaseAuth = Firebase.auth

    /** Emits the current Firebase user (null = signed out). Stays live across sign-in events. */
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val isSignedIn: Boolean get() = auth.currentUser != null
    val uid: String? get() = auth.currentUser?.uid

    // ── Email / password ─────────────────────────────────────────────────────

    suspend fun signInWithEmail(email: String, password: String): AuthResult = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        AuthResult.Success(result.user!!)
    }.getOrElse { AuthResult.Failure(it as Exception) }

    suspend fun signUpWithEmail(email: String, password: String): AuthResult = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        AuthResult.Success(result.user!!)
    }.getOrElse { AuthResult.Failure(it as Exception) }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    // ── Google ────────────────────────────────────────────────────────────────

    /** Call after obtaining idToken from the Credential Manager / Google Sign-In flow. */
    suspend fun signInWithGoogle(idToken: String): AuthResult = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        AuthResult.Success(result.user!!)
    }.getOrElse { AuthResult.Failure(it as Exception) }

    // ── Apple ─────────────────────────────────────────────────────────────────

    /**
     * Initiates Apple Sign-In via Firebase's built-in OAuthProvider.
     * Call from an Activity context — Firebase opens a Custom Tab for Apple's web flow.
     * TODO: wire [activity] from the calling composable via LocalActivity.current
     */
    suspend fun signInWithApple(activity: android.app.Activity): AuthResult = runCatching {
        val provider = OAuthProvider.newBuilder("apple.com")
            .setScopes(listOf("email", "name"))
            .build()
        val result = auth.startActivityForSignInWithProvider(activity, provider).await()
        AuthResult.Success(result.user!!)
    }.getOrElse { AuthResult.Failure(it as Exception) }

    // ── Sign out ──────────────────────────────────────────────────────────────

    fun signOut() = auth.signOut()
}
