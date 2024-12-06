package com.dhkim139.wheretogo

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasourceimpl.CheckPointLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CheckPointRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.LikeRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.RouteRemoteDatasourceImpl
import com.wheretogo.data.di.ApiServiceModule
import com.wheretogo.data.di.DaoDatabaseModule
import com.wheretogo.data.di.RetrofitClientModule
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.repositoryimpl.CourseRepositoryImpl
import com.wheretogo.domain.model.map.Course
import kotlinx.coroutines.delay
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
    fun courseDatasourceTest(): Unit = runBlocking {
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
        //assertEquals(cs1.courseId, csg1.first().courseId)
    }

    @Test
    fun repositoryGetCourseGroupByGeoHashTest(): Unit = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val firestore = FirebaseModule.provideFirestore()
        val naverApi = ApiServiceModule.provideNaverMapApiService(RetrofitClientModule.run {
            provideRetrofit(provideMoshi(), provideClient())
        })
        val courseDao =
            DaoDatabaseModule.run { provideCourseDao(provideCourseDatabase(appContext)) }

        val courseRemote = CourseRemoteDatasourceImpl(firestore)
        val courseLocal = CourseLocalDatasourceImpl(courseDao)
        val routeRemote = RouteRemoteDatasourceImpl(firestore, naverApi)
        val likeRemote = LikeRemoteDatasourceImpl(firestore)
        val checkPointRemote = CheckPointRemoteDatasourceImpl(firestore)
        val checkPointLocal = CheckPointLocalDatasourceImpl()

        val courseRepository = CourseRepositoryImpl(
            courseRemoteDatasource = courseRemote,
            courseLocalDatasource = courseLocal,
            routeRemoteDatasource = routeRemote,
            likeRemoteDatasource = likeRemote,
            checkPointRemoteDatasource = checkPointRemote,
            checkPointLocalDatasource = checkPointLocal
        )
        val dc1 = Course(
            courseId = "cs1"
        )
        val csg1 = courseRepository.getCourseGroupByGeoHash("wyd7")
        assertEquals(1, csg1.size)
        assertEquals(dc1.courseId, csg1.first().courseId)

        delay(1000)
        val csg2 = courseRepository.getCourseGroupByGeoHash("wyd7")
        assertEquals(1, csg1.size)
        assertEquals(dc1.courseId, csg2.first().courseId)
    }
}