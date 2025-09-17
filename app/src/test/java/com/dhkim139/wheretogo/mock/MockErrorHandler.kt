package com.dhkim139.wheretogo.mock

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.ViewModelErrorHandler
import com.wheretogo.presentation.toAppError

class MockErrorHandler : ViewModelErrorHandler {
    override suspend fun handle(error: AppError): AppError = error.toAppError()

    override suspend fun handle(response: UseCaseResponse<*>): AppError = response.toAppError()
}