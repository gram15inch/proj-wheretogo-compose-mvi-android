package com.wheretogo.presentation.model

import com.wheretogo.domain.model.map.LatLng

data class PathInfo (
    val contentId :String,
    val points : List<LatLng>
)