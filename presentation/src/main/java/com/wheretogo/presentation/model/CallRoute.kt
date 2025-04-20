package com.wheretogo.presentation.model

import com.wheretogo.domain.model.map.LatLng

data class CallRoute(
    val start: LatLng,
    val mid: List<LatLng>,
    val goal: LatLng,
    val isMyLocaltionStart: Boolean
)