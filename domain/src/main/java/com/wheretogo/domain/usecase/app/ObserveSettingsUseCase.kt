package com.wheretogo.domain.usecase.app

import com.wheretogo.domain.model.app.Settings
import kotlinx.coroutines.flow.Flow

interface ObserveSettingsUseCase {
    suspend operator fun invoke(): Flow<Result<Settings>>
}