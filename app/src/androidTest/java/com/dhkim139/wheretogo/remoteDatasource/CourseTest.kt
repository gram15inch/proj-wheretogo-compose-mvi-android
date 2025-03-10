package com.dhkim139.wheretogo.remoteDatasource

import android.util.Log
import com.wheretogo.data.datasourceimpl.CourseRemoteDatasourceImpl
import com.wheretogo.data.model.course.RemoteCourse
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import javax.inject.Inject

@HiltAndroidTest
class CourseTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var datasource: CourseRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun getCourseDatasourceTest(): Unit = runBlocking {
        val cs2 = datasource.getCourse("cs1")!!
        assertEquals(true, cs2.dataMetaCheckPoint.checkPointIdGroup.isNotEmpty())
        Log.d("tst5", "${cs2.dataMetaCheckPoint}")
    }


    @Test
    fun getCourseGroupByGeoHashTest(): Unit = runBlocking {
        val cs1 = RemoteCourse(
            courseId = "cs1",
        )
        val start = "wyd7"
        val end = "$start\uf8ff"
        val csg1 = datasource.getCourseGroupByGeoHash(start, end)
        assertEquals(true, csg1.isNotEmpty())
        assertNotEquals(0, csg1.first().dataMetaCheckPoint.checkPointIdGroup.size)
    }

    @Test
    fun removeCourseTest(): Unit = runBlocking {
        val local = RemoteCourse(
            courseId = "cs10"
        )

        datasource.setCourse(local)
        val server = datasource.getCourse(local.courseId)
        assertEquals(local, server)
        datasource.removeCourse(server!!.courseId)

        assertEquals(null, datasource.getCourse(local.courseId))
    }


}