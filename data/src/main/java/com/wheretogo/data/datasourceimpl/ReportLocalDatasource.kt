package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.ReportLocalDatasource
import com.wheretogo.data.datasourceimpl.database.ReportDao
import com.wheretogo.data.datasourceimpl.database.ReportDatabase
import com.wheretogo.data.model.report.LocalReport
import jakarta.inject.Inject

class ReportLocalDatasourceImpl @Inject constructor(
    private val reportDatabase :ReportDatabase
) : ReportLocalDatasource {
    private val reportDao :ReportDao by lazy { reportDatabase.reportDao() }

    override suspend fun addReport(report: LocalReport){
        reportDao.insert(report)
    }
    override suspend fun getReport(reportId: String): LocalReport?{
        return reportDao.select(reportId)
    }
    override suspend fun removeReport(reportId: String){
        reportDao.delete(reportId)
    }
}