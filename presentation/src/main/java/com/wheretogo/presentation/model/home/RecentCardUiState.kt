package com.wheretogo.presentation.model.home

import androidx.compose.runtime.Immutable
import com.wheretogo.domain.RecentCardSituation

@Immutable
data class RecentCardUiState(
    val imageModel: String?,
    val stampAt: Long?,
    val situation: RecentCardSituation = RecentCardSituation.RECENT,
) {
    val isEmpty: Boolean get() = imageModel == null && stampAt == null

    companion object {
        val Empty = RecentCardUiState(imageModel = null, stampAt = null, situation = RecentCardSituation.EMPTY)
    }
}