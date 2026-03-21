package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.DataBuildConfig
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.GuestApiService
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.feature.safeApiCall
import com.wheretogo.data.model.content.ContentFilterRequest
import com.wheretogo.data.model.content.Filter
import com.wheretogo.data.model.content.Operator
import com.wheretogo.data.model.course.RemoteCourse
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CourseRemoteDatasourceImpl @Inject constructor(
    buildConfig: DataBuildConfig,
    private val guestApiService: GuestApiService
) : CourseRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val courseRootCollection = buildConfig.dbPrefix + FireStoreCollections.COURSE.name
    private val keywordAttr = RemoteCourse::keyword.name
    private val updateAtAttr = RemoteCourse::updateAt.name

    override suspend fun getCourse(courseId: String): Result<RemoteCourse> {
        return runCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(courseRootCollection).document(courseId).get()
                    .addOnSuccessListener {
                        continuation.resume(it)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
            snapshot.toObject(RemoteCourse::class.java)
        }.mapDataError()
    }

    override suspend fun getCourseGroupByKeyword(keyword: String): Result<List<RemoteCourse>> {
        return runCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(courseRootCollection)
                    .whereArrayContains(keywordAttr, keyword)
                    .limit(5)
                    .get()
                    .addOnSuccessListener {
                        continuation.resume(it)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
            snapshot.toObjects(RemoteCourse::class.java)
        }
    }

    override suspend fun getCourseGroupByUpdateAt(updateAt: Long): Result<List<RemoteCourse>> {
        return safeApiCall {
            guestApiService.getCourseByFilter(
                ContentFilterRequest(
                    filters = listOf(
                        Filter(
                            field = updateAtAttr,
                            operator = Operator.GREATER_THAN,
                            value = updateAt
                        )
                    )
                )
            )
        }.mapDataError()
    }

    override suspend fun setCourse(course: RemoteCourse): Result<Unit> {
        return runCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(courseRootCollection).document(course.courseId)
                    .set(course)
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
        }
    }

    override suspend fun removeCourse(courseId: String): Result<Unit> {
        return runCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(courseRootCollection).document(courseId).delete()
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
        }
    }
}