package com.wheretogo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.model.journey.LocalCourse
import com.wheretogo.data.model.toCourse
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.repository.CourseRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CourseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CourseRepository {

    override suspend fun getCourse():List<Course>
        = suspendCancellableCoroutine{continuation->
            firestore.collection("courses").get()
                .addOnSuccessListener { result ->
                    val courseGroup = mutableListOf<Course>()
                    result.documents.forEach {
                        it.toObject(LocalCourse::class.java)?.let { course->
                            courseGroup.add(course.toCourse())
                        }
                    }
                    continuation.resume(courseGroup)
                }.addOnFailureListener {
                    continuation.resumeWithException(Exception("No document found"))
                }
        }
}

