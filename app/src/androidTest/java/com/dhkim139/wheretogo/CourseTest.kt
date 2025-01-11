package com.dhkim139.wheretogo

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasourceimpl.CheckPointLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CheckPointRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.RouteRemoteDatasourceImpl
import com.wheretogo.data.di.ApiServiceModule
import com.wheretogo.data.di.DaoDatabaseModule
import com.wheretogo.data.di.RetrofitClientModule
import com.wheretogo.data.model.course.DataMetaCheckPoint
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.repositoryimpl.CheckPointRepositoryImpl
import com.wheretogo.data.repositoryimpl.CourseRepositoryImpl
import com.wheretogo.data.toRemoteCourse
import com.wheretogo.domain.model.dummy.getCourseDummy
import com.wheretogo.domain.model.map.Course
import kotlinx.coroutines.delay
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
        getCourseDummy().forEach { course ->
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
        //assertEquals(cs1.courseId, csg1.first().courseId)
    }

    @Test
    fun repositoryGetCourseTest(): Unit = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val firestore = FirebaseModule.provideFirestore()
        val naverApi = ApiServiceModule.provideNaverMapApiService(RetrofitClientModule.run {
            provideRetrofit(provideMoshi(), provideClient())
        })
        val courseDao =
            DaoDatabaseModule.run { provideCourseDao(provideCourseDatabase(appContext)) }
        val courseRepository = CourseRepositoryImpl(
            courseRemoteDatasource = CourseRemoteDatasourceImpl(firestore),
            courseLocalDatasource = CourseLocalDatasourceImpl(courseDao),
            routeRemoteDatasource = RouteRemoteDatasourceImpl(firestore, naverApi)
        )
        val dc1 = Course(
            courseId = "cs1",
        )
        val cs1 = courseRepository.getCourse(dc1.courseId)
        val cpg1 = cs1?.checkpointIdGroup ?: emptyList()
        Log.d("tst5", "${cpg1.size}")
        assertEquals(true, cpg1.isNotEmpty())
    }

    @Test
    fun repositoryGetCourseGroupByGeoHashTest(): Unit = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val firestore = FirebaseModule.provideFirestore()
        val firebaseStorage = FirebaseModule.provideFirebaseStorage()
        val naverApi = ApiServiceModule.provideNaverMapApiService(RetrofitClientModule.run {
            provideRetrofit(provideMoshi(), provideClient())
        })
        val courseDao =
            DaoDatabaseModule.run { provideCourseDao(provideCourseDatabase(appContext)) }
        val checkPointDao =
            DaoDatabaseModule.run { provideCheckPointDao(provideCheckPointDatabase(appContext)) }
        val checkPointRepository = CheckPointRepositoryImpl(
            checkPointRemoteDatasource = CheckPointRemoteDatasourceImpl(firestore),
            checkPointLocalDatasource = CheckPointLocalDatasourceImpl(checkPointDao)
        )
        val courseRepository = CourseRepositoryImpl(
            courseRemoteDatasource = CourseRemoteDatasourceImpl(firestore),
            courseLocalDatasource = CourseLocalDatasourceImpl(courseDao),
            routeRemoteDatasource = RouteRemoteDatasourceImpl(firestore, naverApi)
        )

        val dc1 = Course(
            courseId = "cs1",
        )
        val csg1 = courseRepository.getCourseGroupByGeoHash("wyd7")
        assertEquals(1, csg1.size)
        assertEquals(dc1.courseId, csg1.first().courseId)
        assertNotEquals(0, csg1.first().checkpointIdGroup.size)

        delay(1000)
        val csg2 = courseRepository.getCourseGroupByGeoHash("wyd7")
        assertEquals(1, csg1.size)
        assertEquals(dc1.courseId, csg2.first().courseId)
    }
}