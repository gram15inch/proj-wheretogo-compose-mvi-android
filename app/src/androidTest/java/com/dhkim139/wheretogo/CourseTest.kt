package com.dhkim139.wheretogo

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.model.journey.LocalCourse
import com.wheretogo.data.model.toCourse
import com.wheretogo.domain.model.map.Course
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CourseTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initializeFirebase() {
            val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                FirebaseApp.initializeApp(appContext)
            }
        }
    }

    @Test
    fun test1() = runBlocking {
        val firestore = FirebaseModule.provideFirestore()

        val course = suspendCancellableCoroutine { continuation ->
            firestore.collection("courses").get()
                .addOnSuccessListener { result ->
                    val courseGroup = mutableListOf<Course>()
                    result.documents.forEach {
                        it.toObject(LocalCourse::class.java)?.let { course ->
                            courseGroup.add(course.toCourse())
                        }
                    }
                    continuation.resume(courseGroup)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }

        assertEquals(7, course.size)
    }

}