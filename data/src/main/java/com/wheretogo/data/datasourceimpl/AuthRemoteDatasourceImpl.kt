package com.wheretogo.data.datasourceimpl

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.domain.model.user.AuthResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRemoteDatasourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRemoteDatasource {
    override suspend fun authWithGoogle(idToken: String): AuthResponse {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.user?.let {
                            continuation.resume(
                                AuthResponse(
                                    isSuccess = true,
                                    data = AuthResponse.AuthData(
                                        uid = it.uid,
                                        id = it.providerId
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