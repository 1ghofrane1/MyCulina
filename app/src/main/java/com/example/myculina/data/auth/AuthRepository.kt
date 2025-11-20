package com.example.myculina.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    companion object {
        private const val TAG = "AuthRepository"
        // ⚠️ REPLACE WITH YOUR WEB CLIENT ID FROM FIREBASE CONSOLE
        private const val WEB_CLIENT_ID = "16453233351-ot4ocnvak3o0c6p0vudc0unp8ql57h4f.apps.googleusercontent.com"
    }

    // Get current user
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Observe auth state changes
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)

        // Send initial value
        trySend(auth.currentUser)

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    // Sign in with Google using Credential Manager (Modern approach)
    suspend fun signInWithGoogle(): Result<FirebaseUser> {
        return try {
            // Generate a nonce for security
            val nonce = generateNonce()
            val hashedNonce = hashNonce(nonce)

            // Configure Google ID option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setNonce(hashedNonce)
                .build()

            // Build credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Get credential from Credential Manager
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            // Handle the credential
            handleSignIn(result, nonce)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    // Handle the sign-in result
    private suspend fun handleSignIn(result: GetCredentialResponse, nonce: String): Result<FirebaseUser> {
        return try {
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        // Extract Google ID Token
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        val idToken = googleIdTokenCredential.idToken

                        Log.d(TAG, "Google ID Token received")

                        // Authenticate with Firebase
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = auth.signInWithCredential(firebaseCredential).await()

                        val user = authResult.user
                        if (user != null) {
                            Log.d(TAG, "Firebase sign-in successful: ${user.displayName}")
                            Result.success(user)
                        } else {
                            Result.failure(Exception("Firebase user is null"))
                        }
                    } else {
                        Log.e(TAG, "Unexpected credential type: ${credential.type}")
                        Result.failure(Exception("Unexpected credential type"))
                    }
                }
                else -> {
                    Log.e(TAG, "Unexpected credential class: ${credential::class.java.name}")
                    Result.failure(Exception("Unexpected credential class"))
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Invalid Google ID token", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in handling failed", e)
            Result.failure(e)
        }
    }

    // Sign out
    fun signOut() {
        auth.signOut()
        Log.d(TAG, "User signed out")
    }

    // Check if user is signed in
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    // Generate a random nonce for security
    private fun generateNonce(): String {
        return UUID.randomUUID().toString()
    }

    // Hash the nonce using SHA-256
    private fun hashNonce(nonce: String): String {
        val bytes = nonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }


    // Register user
    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) Result.success(user)
            else Result.failure(Exception("User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login user
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) Result.success(user)
            else Result.failure(Exception("User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}