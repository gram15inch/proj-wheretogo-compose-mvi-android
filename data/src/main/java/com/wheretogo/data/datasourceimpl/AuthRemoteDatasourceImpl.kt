package com.wheretogo.data.datasourceimpl

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.model.auth.AuthToken
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.domain.model.user.AuthResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRemoteDatasourceImpl @Inject constructor() : AuthRemoteDatasource {
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override suspend fun authGoogleWithFirebase(authToken: AuthToken): AuthResponse {
        val credential = GoogleAuthProvider.getCredential(authToken.idToken, null)
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.user?.let {
                            continuation.resume(
                                AuthResponse(
                                    isSuccess = true,
                                    data = AuthProfile(
                                        uid = it.uid,
                                        email = it.email ?: "",
                                        userName = it.displayName ?: "",
                                        authCompany = AuthCompany.GOOGLE
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

    private fun checkUserStatus() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            Log.d("tst4", "UID: ${currentUser.uid}")
        } else {
            Log.d("tst4", "logout")
        }
    }
}