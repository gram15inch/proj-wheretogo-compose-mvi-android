package com.wheretogo.domain.usecase

interface UserSignInUseCase {
    suspend operator fun invoke(idToken: String)

    suspend fun signPass()
}