package com.wheretogo.presentation.feature.geo

import com.wheretogo.domain.model.address.LatLng

interface LocationService {
    fun distance(form: LatLng, to: LatLng): Int
    fun distanceFloat(form: LatLng, to: LatLng): Float
}