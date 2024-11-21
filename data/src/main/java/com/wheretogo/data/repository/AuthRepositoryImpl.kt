package com.wheretogo.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.wheretogo.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl@Inject constructor(private val firebaseAuth: FirebaseAuth) : AuthRepository {
    override suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("tst4", "UID :${task.result.user?.uid}")
                    task.result
                } else {
                    Log.w("tst4", "Google sign-in failed", task.exception)
                }
            }
    }

    override suspend fun signOutWithGoogle() {
        checkUserStatus()
        firebaseAuth.signOut()
    }

    private fun checkUserStatus() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            Log.d("tst4","UID: ${currentUser.uid}")
        } else {
            Log.d("tst4","logout")
        }
    }
}