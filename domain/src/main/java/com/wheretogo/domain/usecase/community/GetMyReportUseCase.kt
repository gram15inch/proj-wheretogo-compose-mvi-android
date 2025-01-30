package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.community.Report

interface GetMyReportUseCase {
    suspend operator fun invoke(): UseCaseResponse<List<Report>>
}