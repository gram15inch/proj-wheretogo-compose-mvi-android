package com.wheretogo.domain.usecase.app

import com.wheretogo.domain.model.app.AppMessage
import kotlinx.coroutines.flow.Flow

interface ObserveMsgUseCase {
    suspend operator fun invoke(): Flow<AppMessage>
}