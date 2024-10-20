package com.dhkim139.wheretogo.woker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wheretogo.domain.usecase.FetchJourneyWithoutPointsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class JourneyUpdateWorker @AssistedInject constructor (
   @Assisted context: Context,
   @Assisted params: WorkerParameters,
   private val fetchJourneyWithoutPointsUseCase: FetchJourneyWithoutPointsUseCase
): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            //todo 권한 확인 추가
            fetchJourneyWithoutPointsUseCase()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}