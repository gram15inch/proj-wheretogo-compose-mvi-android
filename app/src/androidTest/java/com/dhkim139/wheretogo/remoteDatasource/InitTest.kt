package com.dhkim139.wheretogo.remoteDatasource

import com.wheretogo.data.datasourceimpl.CheckPointRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.RouteRemoteDatasourceImpl
import com.wheretogo.data.model.course.DataMetaCheckPoint
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.data.toRemoteCheckPoint
import com.wheretogo.data.toRemoteCourse
import com.wheretogo.domain.model.dummy.getCheckPointDummy
import com.wheretogo.domain.model.dummy.getCourseDummy
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class InitTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var courseDatasourceImpl: CourseRemoteDatasourceImpl

    @Inject
    lateinit var checkPointRemoteDatasourceImpl: CheckPointRemoteDatasourceImpl

    @Inject
    lateinit var routeRemoteDatasourceImpl: RouteRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

     @Test
     fun courseInit(): Unit = runBlocking {
         val courseGroup = getCourseDummy()
         courseGroup.forEach { course ->
             val r = courseDatasourceImpl.setCourse(
                 course.toRemoteCourse().copy(
                     dataMetaCheckPoint = DataMetaCheckPoint(
                         course.checkpointIdGroup,
                         timeStamp = System.currentTimeMillis()
                     )
                 )
             )
             assertEquals(true, r)
         }
         val cs1 = courseGroup.first()
         assertEquals(cs1.courseId, courseDatasourceImpl.getCourse(cs1.courseId)!!.courseId)
     }

    @Test
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
    }

    @Test
    fun initCheckPoint(): Unit = runBlocking {
        val datasource = checkPointRemoteDatasourceImpl

        getCourseDummy().forEach {course->
            getCheckPointDummy(course.courseId).map { checkpoint ->
                when(course.courseId){
                    getCourseDummy()[0].courseId->{
                        checkpoint.copy(
                            titleComment = "\uD83D\uDE0A 주위가 조용해요.",
                            imageName = "photo_original.jpg"
                        )
                    }
                    getCourseDummy()[1].courseId->{
                        checkpoint.copy(
                            titleComment = "\uD83D\uDE0C 경치가 좋아요.",
                            imageName = "photo_original.jpg"
                        )
                    }
                    getCourseDummy()[5].courseId->{
                        checkpoint.copy(
                            titleComment = "\uD83D\uDE1A 또 가고싶어요.",
                            imageName = "photo_original.jpg"
                        )
                    }
                    else -> checkpoint
                }
            }.forEach {
                datasource.setCheckPoint(it.toRemoteCheckPoint())
            }
        }
    }
}