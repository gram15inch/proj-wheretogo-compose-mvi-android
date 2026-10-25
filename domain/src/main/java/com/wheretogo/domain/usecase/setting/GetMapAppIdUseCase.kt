package com.wheretogo.domain.usecase.setting

fun interface GetMapAppIdUseCase {
    suspend operator fun invoke(): String?
}
