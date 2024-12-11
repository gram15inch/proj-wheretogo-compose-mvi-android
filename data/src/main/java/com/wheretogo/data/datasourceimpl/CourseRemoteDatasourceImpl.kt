package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreTableName
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.name
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CourseRemoteDatasourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CourseRemoteDatasource {
    private val courseTable = FireStoreTableName.COURSE_TABLE.name()
    private val geoHashAttr = "geoHash" // RemoteCourse

    override suspend fun getCourse(courseId: String): RemoteCourse? {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(courseTable).document(courseId)
                .get()
                .addOnSuccessListener {
                    val course = it.toObject(RemoteCourse::class.java)
                    continuation.resume(course)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun getCourseGroupByGeoHash(start: String, end: String): List<RemoteCourse> {

        return suspendCancellableCoroutine { continuation ->
            firestore.collection(courseTable)
                .whereGreaterThanOrEqualTo(geoHashAttr, start)
                .whereLessThan(geoHashAttr, end)
                .get()
                .addOnSuccessListener {
                    val data = it.toObjects(RemoteCourse::class.java)
                    continuation.resume(data)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun setCourse(course: RemoteCourse): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(courseTable).document(course.courseId)
                .set(course)
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }

    override suspend fun removeCourse(courseId: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(courseTable).document(courseId)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }
}