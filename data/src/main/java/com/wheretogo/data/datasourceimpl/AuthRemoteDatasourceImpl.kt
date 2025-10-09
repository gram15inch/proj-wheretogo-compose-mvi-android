package com.wheretogo.data.datasourceimpl

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.toDataError
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.model.auth.AuthToken
import com.wheretogo.domain.model.user.AuthProfile
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRemoteDatasourceImpl @Inject constructor() : AuthRemoteDatasource {
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override suspend fun authGoogleWithFirebase(authToken: AuthToken): Result<AuthProfile> {
        return runCatching {
            val credential = GoogleAuthProvider.getCredential(authToken.idToken, null)
            val user = suspendCancellableCoroutine { continuation ->
                firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener { task ->
                        val user = task.user
                        if (user == null) {
                            continuation.resumeWithException(DataError.ServerError())
                            return@addOnSuccessListener
                        }
                        continuation.resume(user)
                    }
                    .addOnFailureListener {
                        continuation.resumeWithException(it.toDataError())
                    }
            }


            val authProfile = suspendCancellableCoroutine { continuation ->
                user.getIdToken(true)
                    .addOnSuccessListener { result ->
                        val token = result.token
                        if (token == null) {
                            continuation.resumeWithException(DataError.ServerError())
                            return@addOnSuccessListener
                        }
                        continuation.resume(
                            AuthProfile(
                                uid = user.uid,
                                email = user.email ?: "",
                                userName = user.displayName ?: "",
                                authCompany = AuthCompany.GOOGLE,
                                token = token,
                            )
                        )
                    }.addOnFailureListener {
                        continuation.resumeWithException(it.toDataError())
                    }
            }

            authProfile
        }
    }

    override suspend fun signOutOnFirebase(): Result<Unit> {
       return dataErrorCatching {
            checkUserStatus()
            firebaseAuth.signOut()
        }
    }

    override suspend fun getApiToken(isForceRefresh: Boolean): Result<String> {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null)
            return Result.failure(DataError.UserInvalid("user not found for token"))

        return suspendCancellableCoroutine<Result<String>> { continuation ->
            currentUser.getIdToken(isForceRefresh).addOnSuccessListener {
                if (it.token == null)
                    continuation.resume(Result.failure(DataError.UserInvalid("user not found for token")))
                else
                    continuation.resume(Result.success(it.token!!))
            }.addOnFailureListener {
                continuation.resume(
                    Result.failure(DataError.UnexpectedException(Exception(it)))
                )
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