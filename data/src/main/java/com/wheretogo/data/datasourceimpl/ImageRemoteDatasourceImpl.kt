package com.wheretogo.data.datasourceimpl


import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.domain.ImageSize
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ImageRemoteDatasourceImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) : ImageRemoteDatasource {

    override suspend fun setImage(uri: Uri, filename: String, size: ImageSize): Boolean {
        val storageRef: StorageReference =
            firebaseStorage.reference.child("image/${size.pathName}/$filename")

        return suspendCancellableCoroutine { con ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    con.resume(true)
                }
                .addOnFailureListener { exception ->
                    con.resumeWithException(exception)
                }
        }
    }

    override suspend fun getImage(localFile: File, filename: String, size: ImageSize): File? {
        val storageRef: StorageReference =
            firebaseStorage.reference.child("image/${size.pathName}/$filename")
        return suspendCancellableCoroutine { con ->
            storageRef.getFile(localFile).addOnSuccessListener { _ ->
                con.resume(localFile)
            }.addOnFailureListener {
                it.printStackTrace()
                con.resume(null)
            }
        }
    }

    override suspend fun removeImage(filename: String, size: ImageSize): Boolean {
        val storageRef: StorageReference =
            firebaseStorage.reference.child("image/${size.pathName}/$filename")

        return suspendCancellableCoroutine { con ->
            storageRef.delete().addOnSuccessListener { _ ->
                con.resume(true)
            }.addOnFailureListener {
                it.printStackTrace()
                con.resume(false)
            }
        }
    }
}