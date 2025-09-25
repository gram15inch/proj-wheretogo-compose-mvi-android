package com.wheretogo.presentation.feature.ads

import com.google.android.gms.ads.nativead.NativeAd

interface AdService {

    suspend fun getAd(): Result<List<NativeAd>>

    fun refreshAd(count: Int)

}