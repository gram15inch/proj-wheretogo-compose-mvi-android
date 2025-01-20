package com.dhkim139.wheretogo.remote

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.LikeObject
import com.wheretogo.data.datasourceimpl.LikeRemoteDatasourceImpl
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.model.course.RemoteLike
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class LikeTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun initializeFirebase() {
            val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                FirebaseApp.initializeApp(appContext)
            }
            assertEquals("com.dhkim139.wheretogo", appContext.packageName)
        }
    }

    @Test
    fun getAndSetAndRemovelikeTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = LikeRemoteDatasourceImpl(firestore)
        val cs1 = RemoteCourse(
            courseId = "cs1",
        )
        val l1 = RemoteLike(
            like = 1,
        )

        assertEquals(true, datasource.setLikeInObject(LikeObject.COURSE_LIKE, cs1.courseId, l1))

        val l2 = datasource.getLikeInObject(LikeObject.COURSE_LIKE, cs1.courseId)
        assertEquals(l1, l2)

        assertEquals(true, datasource.removeLikeInCourse(LikeObject.COURSE_LIKE, cs1.courseId))

        val l3 = datasource.getLikeInObject(LikeObject.COURSE_LIKE, cs1.courseId)

        assertEquals(0, l3.like)
    }
}