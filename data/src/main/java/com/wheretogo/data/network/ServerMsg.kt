package com.wheretogo.data.network

import com.google.firebase.messaging.FirebaseMessaging
import com.wheretogo.data.DataError
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ServerMsg @Inject constructor() {
    suspend fun getToken(): Result<String> {
        return runCatching {
            suspendCancellableCoroutine { continuation ->
                FirebaseMessaging.getInstance().token
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

}