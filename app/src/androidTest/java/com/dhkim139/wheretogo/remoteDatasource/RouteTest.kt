package com.dhkim139.wheretogo.remoteDatasource

import com.wheretogo.data.datasourceimpl.AddressRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.RouteRemoteDatasourceImpl
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.domain.model.dummy.getCourseDummy
import com.wheretogo.domain.model.map.LatLng
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class RouteTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var courseDatasourceImpl: CourseRemoteDatasourceImpl

    @Inject
    lateinit var routeRemoteDatasourceImpl: RouteRemoteDatasourceImpl

    @Inject
    lateinit var addressRemoteDatasourceImpl: AddressRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun getAndSetRouteTest(): Unit = runBlocking {
        val routeDatasource = routeRemoteDatasourceImpl
        val cs = RemoteCourse(courseId = "cstr1")
        val localRt = RemoteRoute(
            courseId = cs.courseId,
            points = listOf(LatLng(1.0, 1.0), LatLng(2.0, 2.0), LatLng(3.0, 3.0))
        )
        courseDatasourceImpl.setCourse(cs)

        assertEquals(true, routeDatasource.setRouteInCourse(localRt))

        val severRt = routeDatasource.getRouteInCourse(cs.courseId)
        assertEquals(localRt, severRt)
        assertEquals(true, routeDatasource.removeRouteInCourse(cs.courseId))

        assertEquals(true, courseDatasourceImpl.removeCourse(cs.courseId))
        assertEquals(null, courseDatasourceImpl.getCourse(cs.courseId))
    }




    /*@Test
    fun initRouteByNaverTest(): Unit = runBlocking {
        val datasource = routeRemoteDatasourceImpl

        val rtGroup = getCourseDummy().map {
            RemoteRoute(
                courseId = it.courseId,
                points = datasource.getRouteByNaver(it.waypoints).points
            )
        }

        assertEquals(true, rtGroup.first().points.isNotEmpty())
        rtGroup.forEach {
            assertEquals(true, datasource.setRouteInCourse(it))
        }

        rtGroup.forEachIndexed { idx, route ->
            assertEquals(route, datasource.getRouteInCourse(route.courseId))
        }
    }*/
}