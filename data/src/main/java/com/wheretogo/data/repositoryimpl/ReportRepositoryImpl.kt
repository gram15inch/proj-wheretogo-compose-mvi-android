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
    private val reportRemoteDatasource: ReportRemoteDatasource,
    private val reportLocalDatasource: ReportLocalDatasource
) : ReportRepository {
    override suspend fun addReport(report: Report): Result<Unit> {
        return runCatching {
            reportRemoteDatasource.addReport(report.toRemoteReport())
            reportLocalDatasource.addReport(report.toLocalReport())
        }
    }

    override suspend fun getReport(reportId: String): Result<Report> {
        return runCatching {
            reportLocalDatasource.getReport(reportId)?.toReport() ?: run {
                val newReport = reportRemoteDatasource.getReport(reportId)?.toReport()
                checkNotNull(newReport) { "report not found: $reportId" }
                reportLocalDatasource.addReport(newReport.toLocalReport())
                newReport
            }
        }
    }

    override suspend fun removeReport(reportId: String): Result<Unit> {
        return runCatching {
            reportRemoteDatasource.removeReport(reportId)
            reportLocalDatasource.removeReport(reportId)
        }
    }

    override suspend fun getReportByType(reportType: ReportType): Result<List<Report>> {
        return runCatching {
            reportRemoteDatasource.getReportByType(reportType.name).map { it.toReport() }
        }
    }

    override suspend fun getReportByStatus(reportStatus: ReportStatus): Result<List<Report>> {
        return runCatching {
            reportRemoteDatasource.getReportByType(reportStatus.name).map { it.toReport() }
        }
    }

    override suspend fun getReportByUid(userId: String): Result<List<Report>> {
        return runCatching {
            reportRemoteDatasource.getReportByUid(userId).map { it.toReport() }.apply {
                forEach { reportLocalDatasource.addReport(it.toLocalReport()) }
            }
        }
    }
}