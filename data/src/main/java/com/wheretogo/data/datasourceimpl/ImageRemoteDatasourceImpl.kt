package com.wheretogo.data.datasourceimpl


import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wheretogo.data.IMAGE_DOWN_MAX_MB
import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.domain.ImageSize
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ImageRemoteDatasourceImpl @Inject constructor() : ImageRemoteDatasource {
    private val firebaseStorage by lazy { FirebaseStorage.getInstance() }

    override suspend fun uploadImage(
        imageByteArray: ByteArray,
        imageId: String,
        size: ImageSize
    ): Result<Unit> {
        return runCatching {
            val storageRef: StorageReference =
                firebaseStorage.reference.child("image/${size.pathName}/$imageId.jpg")
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
    }

    override suspend fun downloadImage(filename: String, size: ImageSize): Result<ByteArray> {
        return runCatching {
            val storageRef: StorageReference =
                firebaseStorage.reference.child("image/${size.pathName}/$filename.jpg")
            suspendCancellableCoroutine { con ->
                storageRef.getBytes(IMAGE_DOWN_MAX_MB.toLong() * 1024 * 1024)
                    .addOnSuccessListener { byteArray ->
                        con.resume(byteArray)
                    }.addOnFailureListener { e ->
                        con.resumeWithException(e)
                    }
            }
        }
    }

    override suspend fun removeImage(filename: String, size: ImageSize): Result<Unit> {
        return runCatching {
            val storageRef: StorageReference =
                firebaseStorage.reference.child("image/${size.pathName}/$filename.jpg")
            suspendCancellableCoroutine { con ->
                storageRef.delete().addOnSuccessListener { _ ->
                    con.resume(Unit)
                }.addOnFailureListener { e ->
                    con.resumeWithException(e)
                }
            }
        }
    }
}