package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.ReportLocalDatasource
import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.toLocalReport
import com.wheretogo.data.toRemoteReport
import com.wheretogo.data.toReport
import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.community.Report
import com.wheretogo.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportRemoteDatasource : ReportRemoteDatasource,
    private val reportLocalDatasource : ReportLocalDatasource
) : ReportRepository {
    override suspend fun addReport(report: Report) {
        reportRemoteDatasource.addReport(report.toRemoteReport())
        reportLocalDatasource.addReport(report.toLocalReport())
    }

    override suspend fun getReport(reportId: String): Report? {
        return reportLocalDatasource.getReport(reportId)?.toReport() ?: run {
            val newReport = reportRemoteDatasource.getReport(reportId)?.toReport()
            newReport?.let { reportLocalDatasource.addReport(it.toLocalReport()) }
            newReport
        }
    }

    override suspend fun removeReport(reportId: String): Boolean {
        return reportRemoteDatasource.removeReport(reportId).apply {
            reportLocalDatasource.removeReport(reportId)
        }
    }

    override suspend fun getReportByType(reportType: ReportType): List<Report> {
        return reportRemoteDatasource.getReportByType(reportType.name).map { it.toReport() }
    }

    override suspend fun getReportByStatus(reportStatus: ReportStatus): List<Report> {
        return reportRemoteDatasource.getReportByType(reportStatus.name).map { it.toReport() }
    }

    override suspend fun getReportByUid(userId: String): List<Report> {
        return reportRemoteDatasource.getReportByUid(userId).map { it.toReport() }.apply {
            forEach { reportLocalDatasource.addReport(it.toLocalReport()) }
        }
    }
}