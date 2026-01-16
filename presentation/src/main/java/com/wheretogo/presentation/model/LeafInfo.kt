package com.wheretogo.presentation.model

import com.wheretogo.domain.model.address.LatLng


data class LeafInfo(
    val id:String,
    val latLng: LatLng,
    val caption:String,
    val thumbnail:String
)