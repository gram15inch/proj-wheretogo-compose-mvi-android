package com.wheretogo.domain.repository

import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.model.app.AppMessage
import com.wheretogo.domain.model.app.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface AppRepository {
    // setting
    suspend fun observeSetting(): Flow<Settings>
    suspend fun getSetting(): Result<Settings>
    suspend fun setTutorialStep(step: DriveTutorialStep): Result<Unit>

    // token
    suspend fun refreshPublicToken(encryptedSignature: String): Result<Unit>
    suspend fun getPublicToken(): Result<String>
    suspend fun getPublicKey(): Result<String>

    // msg
    val msg: SharedFlow<AppMessage>
    suspend fun sendMsg(msg:AppMessage): Result<Unit>
}