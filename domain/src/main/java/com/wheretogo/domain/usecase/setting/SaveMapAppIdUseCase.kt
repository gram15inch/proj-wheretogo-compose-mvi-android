package com.wheretogo.domain.usecase.setting

fun interface SaveMapAppIdUseCase {
    suspend operator fun invoke(id: String)
}