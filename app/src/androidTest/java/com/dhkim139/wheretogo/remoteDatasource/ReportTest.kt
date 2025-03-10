package com.dhkim139.wheretogo.remoteDatasource

import android.util.Log
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
    val tag = "tst_report"
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var reportRemoteDatasourceImpl: ReportRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun get_set_remove_should_work_correctly(): Unit = runBlocking {
        val rp = RemoteReport(
            reportId = "rp1",
            type = ReportType.COMMENT.name,
            userId = "uid1",
            contentId = "cm1",
        )
        assertEquals(null, reportRemoteDatasourceImpl.getReport(rp.reportId))
        reportRemoteDatasourceImpl.addReport(rp)
        assertEquals(rp, reportRemoteDatasourceImpl.getReport(rp.reportId))
        reportRemoteDatasourceImpl.addReport(rp)
        assertEquals(rp, reportRemoteDatasourceImpl.getReport(rp.reportId))
        reportRemoteDatasourceImpl.removeReport(rp.reportId)
        assertEquals(null, reportRemoteDatasourceImpl.getReport(rp.reportId))
    }

    @Test
    fun getReportByUidTest(): Unit = runBlocking {
        val rpGroup = listOf(
            RemoteReport(reportId = "rp1", userId = "uid1"),
            RemoteReport(reportId = "rp2", userId = "uid2"),
            RemoteReport(reportId = "rp3", userId = "uid1")
        )
        rpGroup.forEach { reportRemoteDatasourceImpl.addReport(it) }

        val uid1 = reportRemoteDatasourceImpl.getReportByUid("uid1")
        val uid2 = reportRemoteDatasourceImpl.getReportByUid("uid2")
        Log.d(tag, uid1.toString())
        assertEquals(2, uid1.size)
        assertEquals(1, uid2.size)

        rpGroup.forEach { reportRemoteDatasourceImpl.removeReport(it.reportId) }
    }
}