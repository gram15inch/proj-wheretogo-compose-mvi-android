package com.wheretogo.domain.usecase.home

import com.wheretogo.domain.model.home.RecentCard
import kotlinx.coroutines.flow.Flow

interface GetRecentCardUseCase {
    operator fun invoke(): Flow<RecentCard>
}