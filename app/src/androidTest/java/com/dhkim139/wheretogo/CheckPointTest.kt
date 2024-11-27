package com.dhkim139.wheretogo

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasource.CheckPointRemoteDatasourceImpl
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import com.wheretogo.data.model.map.DataLatLng
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class CheckPointTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun initializeFirebase() {
            val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                FirebaseApp.initializeApp(appContext)
            }
        }
    }

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.dhkim139.wheretogo", appContext.packageName)
    }


    @Test
    fun checkPointTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CheckPointRemoteDatasourceImpl(firestore)

        val cp1 = RemoteCheckPoint(
            checkPointId = "cp1",
            latLng = DataLatLng(123.321, 123.456),
            titleComment = "cp1 comment",
            imgUrl = "https://testImg12312312312.com/test"
        )

        assertEquals(true, datasource.setCheckPoint(cp1))

        val cp2 = datasource.getCheckPoint(cp1.checkPointId)
        assertEquals(cp2, cp1)
    }

}