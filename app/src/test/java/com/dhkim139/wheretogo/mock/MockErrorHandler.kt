package com.dhkim139.wheretogo.mock

import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.ViewModelErrorHandler
import com.wheretogo.presentation.toAppError

class MockErrorHandler : ViewModelErrorHandler {
    override suspend fun handle(error: Throwable): Throwable = error.toAppError()
}