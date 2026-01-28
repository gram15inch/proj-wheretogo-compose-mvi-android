package com.wheretogo.data.datasourceimpl

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.toDataError
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.model.auth.SignToken
import com.wheretogo.domain.model.auth.SyncProfile
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRemoteDatasourceImpl @Inject constructor() : AuthRemoteDatasource {
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseMsg by lazy { FirebaseMessaging.getInstance() }

    override suspend fun authGoogleWithFirebase(signToken: SignToken): Result<SyncProfile> {
        return runCatching {
            val credential = GoogleAuthProvider.getCredential(signToken.token, null)
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

            val idToken = suspendCancellableCoroutine { continuation ->
                user.getIdToken(true)
                    .addOnSuccessListener { result ->
                        val idToken = result.token
                        if (idToken == null) {
                            continuation.resumeWithException(DataError.ServerError())
                            return@addOnSuccessListener
                        }
                        continuation.resume(idToken)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it.toDataError())
                    }
            }
            val msgToken = getMsgToken().getOrNull() ?: ""

            SyncProfile(
                uid = user.uid,
                mail = user.email ?: "",
                name = user.displayName ?: "",
                authCompany = AuthCompany.GOOGLE,
                idToken = idToken,
                msgToken = msgToken
            )
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

    override suspend fun getMsgToken(): Result<String> {
        return runCatching {
            suspendCancellableCoroutine { continuation ->
                firebaseMsg.token
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            val err = task.exception
                            if (err != null)
                                return@addOnCompleteListener continuation.resumeWithException(err)
                            else
                                return@addOnCompleteListener continuation.resumeWithException(
                                    DataError.InternalError("Empty exception")
                                )
                        }
                        continuation.resume(task.result)
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