package com.dhkim139.wheretogo.remoteDatasource

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasourceimpl.ReportRemoteDatasourceImpl
import com.wheretogo.data.model.report.RemoteReport
import com.wheretogo.domain.ReportType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ReportTest {

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
    fun getAndAddReportTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = ReportRemoteDatasourceImpl(firestore)
        val rp = RemoteReport(
            reportId = "rp1",
            type = ReportType.COMMENT.name,
            userId = "uid1",
            contentId = "cm1",
        )
        datasource.addReport(rp)

        assertEquals(rp, datasource.getReport(rp.reportId))
    }

    @Test
    fun removeReportTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = ReportRemoteDatasourceImpl(firestore)
        val rp = RemoteReport(
            reportId = "rp1",
            type = ReportType.COMMENT.name,
            userId = "uid1",
            contentId = "cm1",
        )
        datasource.addReport(rp)

        assertEquals(rp, datasource.getReport(rp.reportId))
    }
}