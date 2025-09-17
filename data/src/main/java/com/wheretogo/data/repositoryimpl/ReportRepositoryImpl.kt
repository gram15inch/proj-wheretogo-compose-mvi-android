package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.ReportLocalDatasource
import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.catchNotFound
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.toDomainResult
import com.wheretogo.data.toLocalReport
import com.wheretogo.data.toRemoteReport
import com.wheretogo.data.toReport
import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.feature.flatMap
import com.wheretogo.domain.model.report.Report
import com.wheretogo.domain.model.report.ReportAddRequest
import com.wheretogo.domain.repository.ReportRepository
import de.huxhorn.sulky.ulid.ULID
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportRemoteDatasource: ReportRemoteDatasource,
    private val reportLocalDatasource: ReportLocalDatasource
) : ReportRepository {
    override suspend fun addReport(request: ReportAddRequest): Result<String> {
        val reportId = "RP${ULID().nextULID()}"
        val remote = request.toRemoteReport(reportId)
        val local = remote.toLocalReport()

        return reportRemoteDatasource.addReport(remote).mapSuccess {
            reportLocalDatasource.addReport(local)
        }.mapCatching { reportId }
            .toDomainResult()
    }

    override suspend fun getReport(reportId: String): Result<Report> {
        return reportLocalDatasource.getReport(reportId)
            .catchNotFound{
                reportRemoteDatasource.getReport(reportId)
                    .mapSuccess { reportLocalDatasource.addReport(it.toLocalReport()) }
                    .mapSuccess { reportLocalDatasource.getReport(reportId) }
            }.mapCatching { it.toReport() }
            .mapDomainError()
    }

    override suspend fun removeReport(reportId: String): Result<Unit> {
        return reportRemoteDatasource.removeReport(reportId).mapSuccess {
            reportLocalDatasource.removeReport(reportId)
        }.toDomainResult()
    }

    override suspend fun getReportByType(reportType: ReportType): Result<List<Report>> {
        return reportRemoteDatasource.getReportByType(reportType.name)
            .mapCatching { it.map { it.toReport() } }
            .mapDomainError()
    }

    override suspend fun getReportByStatus(reportStatus: ReportStatus): Result<List<Report>> {
        return reportRemoteDatasource.getReportByType(reportStatus.name)
            .mapCatching { it.map { it.toReport() } }
            .mapDomainError()
    }

    override suspend fun getReportByUid(userId: String): Result<List<Report>> {
        return reportRemoteDatasource.getReportByUid(userId)
            .mapSuccess {
                it.map { remote ->
                    val local = remote.toLocalReport()
                    reportLocalDatasource.addReport(local)
                        .mapCatching { local }
                }.flatMap()
            }.mapCatching { it.map { it.toReport() } }
            .mapDomainError()
    }
}