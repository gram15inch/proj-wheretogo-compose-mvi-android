package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.HistoryType

interface RemoveHistoryUseCase {
    suspend operator fun invoke(historyId: String, type: HistoryType)
}