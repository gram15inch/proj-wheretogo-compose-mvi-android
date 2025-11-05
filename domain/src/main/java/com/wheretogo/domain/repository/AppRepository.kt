package com.wheretogo.domain.repository

import com.wheretogo.domain.TutorialStep
import com.wheretogo.domain.model.app.Settings
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    // setting
    suspend fun observeSetting(): Flow<Settings>
    suspend fun getSetting(): Result<Settings>
    suspend fun setTutorialStep(step: TutorialStep): Result<Unit>

    // token
    suspend fun refreshPublicToken(encryptedSignature: String): Result<Unit>
    suspend fun getPublicToken(): Result<String>
    suspend fun getPublicKey(): Result<String>
}