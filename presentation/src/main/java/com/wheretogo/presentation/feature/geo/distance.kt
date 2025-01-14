package com.wheretogo.presentation.feature.geo

import android.location.Location
import com.wheretogo.domain.model.map.LatLng


fun LatLng.distanceTo(latLng: LatLng): Int { // meter
    return FloatArray(1).apply {
        Location.distanceBetween(
            latitude,
            longitude,
            latLng.latitude,
            latLng.longitude,
            this
        )
    }[0].toInt()
}

