package com.dhkim139.wheretogo.remoteDatasource

import com.wheretogo.data.datasourceimpl.ReportRemoteDatasourceImpl
import com.wheretogo.data.model.report.RemoteReport
import com.wheretogo.domain.ReportType
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import javax.inject.Inject

@HiltAndroidTest
class ReportTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var reportRemoteDatasourceImpl: ReportRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun getAndAddReportTest(): Unit = runBlocking {
        val datasource = reportRemoteDatasourceImpl
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
        val datasource = reportRemoteDatasourceImpl
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