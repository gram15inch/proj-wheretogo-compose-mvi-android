package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.toReport
import com.wheretogo.data.toReportRequest
import com.wheretogo.domain.model.report.Report
import com.wheretogo.domain.model.report.ReportAddRequest
import com.wheretogo.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportRemoteDatasource: ReportRemoteDatasource
) : ReportRepository {
    override suspend fun addReport(request: ReportAddRequest): Result<String> {
        val request = request.toReportRequest()
        return reportRemoteDatasource.addReport(request)
    }

    override suspend fun getReport(reportId: String): Result<Report> {
        return reportRemoteDatasource.getReport(reportId).map { it.toReport() }
    }
}