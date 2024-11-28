package com.dhkim139.wheretogo

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasourceimpl.CourseRemoteDatasourceImpl
import com.wheretogo.data.model.course.RemoteCourse
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class CourseTest {

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
    fun courseTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CourseRemoteDatasourceImpl(firestore)
        val cs1 = RemoteCourse(
            courseId = "cs1",
        )

        assertEquals(true, datasource.setCourse(cs1))

        val cs2 = datasource.getCourse(cs1.courseId)

        assertEquals(cs1, cs2)

        assertEquals(true, datasource.removeCourse(cs1.courseId))

        val cs3 = datasource.getCourse(cs1.courseId)
        assertEquals(null, cs3)
    }

}