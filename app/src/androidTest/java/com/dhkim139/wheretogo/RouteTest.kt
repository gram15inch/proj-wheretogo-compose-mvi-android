package com.dhkim139.wheretogo

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasource.RouteRemoteDatasourceImpl
import com.wheretogo.data.model.route.RemoteRoute
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
        val datasource = RouteRemoteDatasourceImpl(firestore)
        val rt1 = RemoteRoute(
            routeId = datasource.getRouteId("cs1")
        )

        assertEquals(true, datasource.setRouteInCourse("cs1", rt1))
        val rt2 = datasource.getRouteInCourse("cs1")

        assertEquals(rt1, rt2)
        assertEquals(true, datasource.removeRouteInCourse("cs1"))

        val rt3 = datasource.getRouteInCourse("cs1")
        assertEquals(true, rt3.points.isEmpty())
    }
}