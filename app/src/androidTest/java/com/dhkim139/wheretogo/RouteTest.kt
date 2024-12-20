package com.dhkim139.wheretogo

import android.os.StrictMode
import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasourceimpl.RouteRemoteDatasourceImpl
import com.wheretogo.data.di.ApiServiceModule
import com.wheretogo.data.di.RetrofitClientModule
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.domain.model.dummy.getCourseDummy
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test


class RouteTest {
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
    fun routeTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val naverApi = ApiServiceModule.provideNaverMapApiService(RetrofitClientModule.run {
            provideRetrofit(provideMoshi(), provideClient())
        })
        val datasource = RouteRemoteDatasourceImpl(firestore, naverApi)
        val rt1 = RemoteRoute(
            courseId = "cs1"
        )

        assertEquals(true, datasource.setRouteInCourse(rt1))
        val rt2 = datasource.getRouteInCourse("cs1")

        assertEquals(rt1, rt2)
        assertEquals(true, datasource.removeRouteInCourse("cs1"))

        val rt3 = datasource.getRouteInCourse("cs1")
        assertEquals(true, rt3.points.isEmpty())
    }

    @Test
    fun getRouteByNaverTest(): Unit = runBlocking {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .permitAll() // 모든 네트워크 작업 허용
                .build()
        )
        val firestore = FirebaseModule.provideFirestore()
        val naverApi = ApiServiceModule.provideNaverMapApiService(RetrofitClientModule.run {
            provideRetrofit(provideMoshi(), provideClient())
        })
        val datasource = RouteRemoteDatasourceImpl(firestore, naverApi)


        val rtGroup = listOf(getCourseDummy()).first().map {
            RemoteRoute(
                courseId = it.courseId,
                points = datasource.getRouteByNaver(it.waypoints)
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
    fun routeSearchPriceTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val naverApi = ApiServiceModule.provideNaverMapApiService(RetrofitClientModule.run {
            provideRetrofit(provideMoshi(), provideClient())
        })

        val datasource = RouteRemoteDatasourceImpl(firestore, naverApi)

        // datasource.getGeoTest(100.0)
        assertEquals(true, datasource.setGeoTest())
        /*    assertNotEquals(0, datasource.getGeoTest(100.0))
            delay(3000)
            assertNotEquals(0, datasource.getGeoTest(150.0))*/
    }
}