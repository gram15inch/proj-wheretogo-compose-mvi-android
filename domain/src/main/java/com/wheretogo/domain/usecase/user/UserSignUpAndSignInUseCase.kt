package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.model.auth.AuthRequest

interface UserSignUpAndSignInUseCase {
    suspend operator fun invoke(authRequest: AuthRequest): Result<String>
}