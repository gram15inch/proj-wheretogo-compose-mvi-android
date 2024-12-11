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

    override suspend fun setCheckPointImage(remotePath: String): String {
        return downloadFileToLocal(context, remotePath, "cache_${remotePath.split("/").last()}")
    }

    private suspend fun downloadFileToLocal(
        context: Context,
        filePath: String,
        localFileName: String,
    ): String {
        val storageRef: StorageReference = firebaseStorage.reference.child(filePath)
        val localFile = File(context.cacheDir, localFileName) // 로컬 캐시 디렉토리에 저장

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