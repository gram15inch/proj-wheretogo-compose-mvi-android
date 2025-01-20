package com.dhkim139.wheretogo.remote

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasourceimpl.CourseRemoteDatasourceImpl
import com.wheretogo.data.model.course.DataMetaCheckPoint
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.toRemoteCourse
import com.wheretogo.domain.model.dummy.getCourseDummy
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
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
    fun courseInit(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CourseRemoteDatasourceImpl(firestore)
        val courseGroup = getCourseDummy()
        courseGroup.forEach { course ->
            val r = datasource.setCourse(
                course.toRemoteCourse().copy(
                    dataMetaCheckPoint = DataMetaCheckPoint(
                        course.checkpointIdGroup,
                        timeStamp = System.currentTimeMillis()
                    )
                )
            )
            assertEquals(true, r)
        }
        val cs1 =courseGroup.first()
        assertEquals(cs1.courseId, datasource.getCourse(cs1.courseId)!!.courseId)
    }


    @Test
    fun getCourseDatasourceTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CourseRemoteDatasourceImpl(firestore)


        val cs2 = datasource.getCourse("cs1")!!
        assertEquals(true, cs2.dataMetaCheckPoint.checkPointIdGroup.isNotEmpty())
        Log.d("tst5", "${cs2.dataMetaCheckPoint}")
    }

    @Test
    fun datasourceGetCourseGroupByGeoHashTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CourseRemoteDatasourceImpl(firestore)
        val cs1 = RemoteCourse(
            courseId = "cs1",
        )
        val start = "wyd7"
        val end = "$start\uf8ff"
        val csg1 = datasource.getCourseGroupByGeoHash(start, end)
        assertEquals(true, csg1.isNotEmpty())
        assertNotEquals(0, csg1.first().dataMetaCheckPoint.checkPointIdGroup.size)
    }
}