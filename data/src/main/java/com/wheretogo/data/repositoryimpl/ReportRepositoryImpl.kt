package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.toRemoteReport
import com.wheretogo.data.toReport
import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.community.Report
import com.wheretogo.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportRemoteDatasource: ReportRemoteDatasource
) : ReportRepository {
    override suspend fun addReport(report: Report) {
        reportRemoteDatasource.addReport(report.toRemoteReport())
    }

    override suspend fun getReport(reportID: String): Report? {
        return reportRemoteDatasource.getReport(reportID)?.toReport()
    }

    override suspend fun getReportByType(reportType: ReportType): List<Report> {
        return reportRemoteDatasource.getReportByType(reportType.name).map { it.toReport() }
    }

    override suspend fun getReportByStatus(reportStatus: ReportStatus): List<Report> {
        return reportRemoteDatasource.getReportByType(reportStatus.name).map { it.toReport() }
    }
}