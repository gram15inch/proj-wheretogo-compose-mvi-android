package com.wheretogo.presentation.model

import com.wheretogo.domain.model.util.LatLngLabel

data class CallRoute(
    val start: LatLngLabel,
    val mid: List<LatLngLabel>,
    val goal: LatLngLabel,
    val isMyLocaltionStart: Boolean
)