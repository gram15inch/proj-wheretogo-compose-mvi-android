package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.model.map.History
import kotlinx.coroutines.flow.Flow

interface GetHistoryStreamUseCase {
    suspend operator fun invoke(): Flow<History>
}