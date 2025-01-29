package com.wheretogo.data.datasourceimpl

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.domain.model.user.AuthData
import com.wheretogo.domain.model.user.AuthResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRemoteDatasourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleIdOption: GetGoogleIdOption,
    @ApplicationContext private val context: Context
) : AuthRemoteDatasource {

    override suspend fun authOnDevice(): AuthResponse {
        return try {
            val credentialManager = CredentialManager.create(context)
            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            getGoogleIdTokenCredential(result.credential)?.let {
                signInWithGoogle(it.idToken)
            } ?: AuthResponse(false)
        } catch (e: Exception) {
            AuthResponse(false)
        }
    }

    override suspend fun signOutOnFirebase() {
        checkUserStatus()
        firebaseAuth.signOut()
    }

    override suspend fun deleteUser(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                continuation.resume(false)
            } else {
                user.delete().addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
            }
        }
    }

    private fun getGoogleIdTokenCredential(credential: Credential): GoogleIdTokenCredential? {
        return when (credential.type) {
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                GoogleIdTokenCredential.createFrom(credential.data)
            }

            else -> null
        }
    }

    private suspend fun signInWithGoogle(idToken: String): AuthResponse {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.user?.let {
                            continuation.resume(
                                AuthResponse(
                                    isSuccess = true,
                                    data = AuthData(
                                        uid = it.uid,
                                        email = it.email ?: "",
                                        userName = it.displayName ?: ""
                                    )
                                )
                            )
                        }

                    } else {
                        continuation.resume(AuthResponse(false))
                    }
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    private fun checkUserStatus() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            Log.d("tst4", "UID: ${currentUser.uid}")
        } else {
            Log.d("tst4", "logout")
        }
    }

    fun generateNonce(): String {
        val nonce = UUID.randomUUID().toString()
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(nonce.toByteArray())
            Base64.encodeToString(hash, Base64.NO_WRAP or Base64.URL_SAFE)
        } catch (e: Exception) {
            nonce
        }
    }
}