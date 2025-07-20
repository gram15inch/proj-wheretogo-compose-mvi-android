package com.wheretogo.presentation.feature.ads

import com.google.android.gms.ads.nativead.NativeAd
import com.wheretogo.presentation.AdLifecycle
import kotlinx.coroutines.flow.Flow


interface AdService {

    val adLifeCycle: Flow<AdLifecycle>

    suspend fun getAd(): Result<List<NativeAd>>

    fun refreshAd()

    fun lifeCycleChange(event:AdLifecycle)

}