package com.wheretogo.data.datasourceimpl

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.model.auth.AuthToken
import com.wheretogo.domain.model.user.AuthProfile
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class AuthRemoteDatasourceImpl @Inject constructor() : AuthRemoteDatasource {
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override suspend fun authGoogleWithFirebase(authToken: AuthToken): AuthProfile? {
        val credential = GoogleAuthProvider.getCredential(authToken.idToken, null)
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    runCatching {
                        val user = task.result.user!!
                        AuthProfile(
                            uid = user.uid,
                            email = user.email ?: "",
                            userName = user.displayName ?: "",
                            authCompany = AuthCompany.GOOGLE
                        )
                    }.onSuccess {
                        continuation.resume(it)
                    }.onFailure {
                        continuation.resume(null)
                    }
                }
        }
    }

    override suspend fun signOutOnFirebase() {
        checkUserStatus()
        firebaseAuth.signOut()
    }

    override suspend fun deleteUser(): Boolean {
        val user = firebaseAuth.currentUser
        return user?.delete()?.isSuccessful ?: false
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