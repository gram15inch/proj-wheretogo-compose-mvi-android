package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.flow.Flow

interface GetUserProfileStreamUseCase {
    suspend operator fun invoke(): Flow<Result<Profile>>
}