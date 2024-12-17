package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.HistoryType

interface UpdateHistoryUseCase {
    suspend operator fun invoke(historyId: String, type: HistoryType)
}