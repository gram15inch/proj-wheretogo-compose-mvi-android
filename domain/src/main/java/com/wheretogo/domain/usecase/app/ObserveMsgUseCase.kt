package com.wheretogo.domain.usecase.app

import com.wheretogo.domain.FcmMsg
import kotlinx.coroutines.flow.Flow

interface ObserveMsgUseCase {
    suspend operator fun invoke(): Flow<FcmMsg>
}