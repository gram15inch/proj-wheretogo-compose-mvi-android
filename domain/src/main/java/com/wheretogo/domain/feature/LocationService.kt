package com.wheretogo.domain.feature

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.util.Viewport

interface LocationService {
    fun distance(form: LatLng, to: LatLng): Int
    fun distanceFloat(form: LatLng, to: LatLng): Float
    fun isContainByViewPort(vp: Viewport, target: LatLng): Boolean
}