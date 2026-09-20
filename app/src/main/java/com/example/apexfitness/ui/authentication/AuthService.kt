package com.example.apexfitness.ui.authentication

import android.content.Context
import com.example.apexfitness.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Wraps Firebase Authentication (email and Google) so the screens never call FirebaseAuth directly.
// Firebase keeps the session on the phone, so the user stays signed in after closing the app.
class AuthService(private val context: Context) {
    // Lazy and guarded so previews do not crash when there is no FirebaseApp
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUser(): FirebaseUser? = try {
        auth?.currentUser
    } catch (e: Exception) {
        null
    }

    // Emits the current user, then again every time the sign-in state changes
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            trySend(null)
            awaitClose { }
        } else {
            val listener = FirebaseAuth.AuthStateListener { current ->
                trySend(current.currentUser)
            }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase is not available"))
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: return Result.failure(IllegalStateException("Account was not created"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase is not available"))
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: return Result.failure(IllegalStateException("Sign in failed"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase is not available"))
        return try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getWebClientId())
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase is not available"))
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(IllegalStateException("Google sign in failed"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // True when the account signs in with an email and password
    fun usesPassword(): Boolean =
        auth?.currentUser?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true

    // Firebase wants a recent sign in before it deletes an account, so the user confirms first
    suspend fun reauthWithPassword(password: String): Result<Unit> {
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = user.email ?: return Result.failure(IllegalStateException("No email on this account"))
        return try {
            user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reauthWithGoogle(idToken: String): Result<Unit> {
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        return try {
            user.reauthenticate(GoogleAuthProvider.getCredential(idToken, null)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCurrentUser(): Result<Unit> {
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        return try {
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth?.signOut()
        getGoogleSignInClient().signOut()
    }

    private fun getWebClientId(): String {
        // Generated by the google-services plugin from google-services.json, so do not hardcode it
        return context.getString(R.string.default_web_client_id)
    }
}
