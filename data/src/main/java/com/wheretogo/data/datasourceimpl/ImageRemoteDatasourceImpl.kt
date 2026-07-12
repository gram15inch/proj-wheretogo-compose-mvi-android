package com.wheretogo.data.datasourceimpl


import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.data.model.confg.ImageConfig
import com.wheretogo.data.model.gallery.ImageMetaCreateContent
import com.wheretogo.data.model.gallery.ImageMetaResponse
import com.wheretogo.domain.ImageSize
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ImageRemoteDatasourceImpl @Inject constructor(
    private val imageConfig: ImageConfig
) : ImageRemoteDatasource {
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val store by lazy { FirebaseFirestore.getInstance() }
    private val rootCollection = FireStoreCollections.IMAGE.name
    private val ext = imageConfig.format.ext
    private val maxConcurrency = 20

    override suspend fun getImageMetasByUserId(userId: String): Result<List<ImageMetaResponse>> {
        return runCatching {
            store.collection(rootCollection)
                .whereEqualTo(ImageMetaResponse::userId.name, userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(ImageMetaResponse::class.java) }
        }
    }

    override suspend fun setImageMetas(metas: List<ImageMetaCreateContent>): Result<List<String>> =
        runCatching {
            coroutineScope {
                val semaphore = Semaphore(maxConcurrency)
                metas.map { meta ->
                    async {
                        semaphore.withPermit {
                            try {
                                store.collection(rootCollection)
                                    .document(meta.imageId)
                                    .set(meta)
                                    .await()
                                null
                            } catch (e: Exception) {
                                Timber.e(e.stackTraceToString())
                                meta.imageId
                            }
                        }
                    }
                }.awaitAll().filterNotNull()
            }
        }

    override suspend fun removeImageMetas(metas: List<String>): Result<List<String>> = runCatching {
        coroutineScope {
            val semaphore = Semaphore(maxConcurrency)
            metas.map { metaId ->
                async {
                    semaphore.withPermit {
                        try {
                            store.collection(rootCollection)
                                .document(metaId)
                                .delete()
                                .await()
                            null
                        } catch (e: Exception) {
                            Timber.e(e.stackTraceToString())
                            metaId
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }


    override suspend fun uploadImageByByteArray(
        imageByteArray: ByteArray,
        imageId: String,
        size: ImageSize
    ): Result<Unit> {
        return runCatching {
            val storageRef: StorageReference =
                storage.reference.child("image/${size.pathName}/$imageId.$ext")
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

    override suspend fun uploadImageByFile(
        imageFile: File,
        imageId: String,
        size: ImageSize
    ): Result<Unit> {
        return runCatching {
            val storageRef: StorageReference =
                storage.reference.child("image/${size.pathName}/$imageId.$ext")
            storageRef.putFile(imageFile.toUri()).await()
        }
    }

    override suspend fun downloadImage(filename: String, size: ImageSize): Result<ByteArray> {
        return runCatching {
            val storageRef: StorageReference =
                storage.reference.child("image/${size.pathName}/$filename.$ext")
            suspendCancellableCoroutine { con ->
                storageRef.getBytes(imageConfig.maxDownMB.toLong() * 1024 * 1024)
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
            val storageRef =
                storage.reference.child("image/${size.pathName}/$filename.$ext")
            storageRef.delete().await()
            Unit
        }
    }
}