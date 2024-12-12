package com.wheretogo.data.datasourceimpl

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wheretogo.data.datasource.ImageLocalDatasource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ImageLocalDatasourceImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : ImageLocalDatasource {

    override suspend fun getImage(fileName: String, size: String): String {
        val remoteChildPath = "image/$size/$fileName"
        val localFile = File(context.cacheDir, "${size}_${fileName}")
        return if(localFile.exists())
            localFile.path
        else
            downloadFileToLocal(remoteChildPath, localFile)
    }

    private suspend fun downloadFileToLocal(
        remoteChildPath: String,
        localFile: File
    ): String {
        val storageRef: StorageReference = firebaseStorage.reference.child(remoteChildPath)

        return suspendCancellableCoroutine<String> { con ->
            storageRef.getFile(localFile)
                .addOnSuccessListener {
                    con.resume(localFile.path)
                }
                .addOnFailureListener { exception ->
                    con.resumeWithException(exception)
                }
        }
    }
}