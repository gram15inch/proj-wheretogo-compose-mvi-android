package com.wheretogo.presentation.feature.ads

import com.wheretogo.presentation.model.AdItem

interface AdService {

    suspend fun getAd(): Result<List<AdItem>>

    fun refreshAd(count: Int)

}