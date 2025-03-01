package com.wheretogo.data.datasourceimpl


import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wheretogo.data.IMAGE_DOWN_MAX_MB
import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.domain.ImageSize
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ImageRemoteDatasourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageRemoteDatasource {
    private val firebaseStorage by lazy { FirebaseStorage.getInstance() }

    override suspend fun uploadImage(
        imageByteArray: ByteArray,
        imageName: String,
        size: ImageSize
    ){
        val storageRef: StorageReference =
            firebaseStorage.reference.child("image/${size.pathName}/$imageName")
        suspendCancellableCoroutine { con ->
            storageRef.putBytes(imageByteArray)
                .addOnSuccessListener {
                    con.resume(Unit)
                }
                .addOnFailureListener { e ->
                    con.resumeWithException(e)
                }
        }
    }

    override suspend fun downloadImage(filename: String, size: ImageSize): ByteArray {
        val storageRef: StorageReference =
            firebaseStorage.reference.child("image/${size.pathName}/$filename")
        return suspendCancellableCoroutine { con ->
            storageRef.getBytes(IMAGE_DOWN_MAX_MB.toLong() * 1024 * 1024)
                .addOnSuccessListener { byteArray ->
                    con.resume(byteArray)
                }.addOnFailureListener { e ->
                con.resumeWithException(e)
            }
        }
    }

    override suspend fun removeImage(filename: String, size: ImageSize) {
        val storageRef: StorageReference =
            firebaseStorage.reference.child("image/${size.pathName}/$filename")

        return suspendCancellableCoroutine { con ->
            storageRef.delete().addOnSuccessListener { _ ->
                con.resume(Unit)
            }.addOnFailureListener { e ->
                con.resumeWithException(e)
            }
        }
    }
}