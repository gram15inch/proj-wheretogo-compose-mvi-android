package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.ReportLocalDatasource
import com.wheretogo.data.datasourceimpl.database.ReportDao
import com.wheretogo.data.datasourceimpl.database.ReportDatabase
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.report.LocalReport
import jakarta.inject.Inject

class ReportLocalDatasourceImpl @Inject constructor(
    private val reportDatabase: ReportDatabase
) : ReportLocalDatasource {
    private val reportDao: ReportDao by lazy { reportDatabase.reportDao() }

    override suspend fun addReport(report: LocalReport): Result<Unit> {
        return dataErrorCatching { reportDao.insert(report) }
    }

    override suspend fun getReport(reportId: String): Result<LocalReport> {
        return dataErrorCatching { reportDao.select(reportId) }
    }

    override suspend fun removeReport(reportId: String): Result<Unit> {
        return dataErrorCatching { reportDao.delete(reportId) }
    }
}