package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.UserStatus

interface UserCheckUseCase {
    suspend operator fun invoke(): Result<UserStatus>
}