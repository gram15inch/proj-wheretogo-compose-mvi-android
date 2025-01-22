package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.community.Report

interface GetReportUseCase {
    suspend operator fun invoke(type: ReportType): List<Report>
}