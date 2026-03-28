package com.wheretogo.domain.usecase.app

interface AppCheckBySignatureUseCase {
    suspend operator fun invoke(): Result<Boolean> // 성공 메세지 토스트 표시할지 여부
}