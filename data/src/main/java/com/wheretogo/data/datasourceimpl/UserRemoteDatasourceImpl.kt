package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.datasource.UserRemoteDatasource

import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRemoteDatasourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRemoteDatasource {
    private val table=  "usersTest"

    override suspend fun setProfile(profile: Profile): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(table).document(profile.uid)
                .set(profile)
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    override suspend fun getProfile(uid:String): Profile? {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(table).document(uid)
                .get()
                .addOnSuccessListener { result ->
                    if (result != null && result.exists()) {
                        val profile = result.toObject(Profile::class.java)
                        if(profile!=null){
                            continuation.resume(profile)
                        }else{
                            continuation.resumeWithException(Exception("User parse error uid:$uid"))
                        }
                    }else{
                        continuation.resume(null)
                    }
                }.addOnFailureListener { e->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    suspend fun removeProfile(uid:String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(table).document(uid)
                .delete()
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

}