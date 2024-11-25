package com.wheretogo.domain.usecase


import com.wheretogo.domain.model.user.Profile

interface UserProfileUpdateUseCase {
    suspend operator fun invoke(profile: Profile)
    suspend fun lastVisitedUpdate()
}