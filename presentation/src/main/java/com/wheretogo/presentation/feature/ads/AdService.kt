package com.wheretogo.presentation.feature.ads

import com.wheretogo.presentation.model.AdItem

interface AdService {

    suspend fun getAd(slotId: String): Result<List<AdItem>>

    fun clearSlot(slotId: String)

    fun disable()
}