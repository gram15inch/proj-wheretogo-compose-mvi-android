package com.wheretogo.presentation.feature.geo

import android.location.Location
import com.wheretogo.domain.model.address.LatLng
import jakarta.inject.Inject

class LocationServiceImpl @Inject constructor() : LocationService {
    override fun distance(
        form: LatLng,
        to: LatLng
    ): Int {
        return FloatArray(1).apply {
            Location.distanceBetween(
                form.latitude,
                form.longitude,
                to.latitude,
                to.longitude,
                this
            )
        }[0].toInt()
    }

    override fun distanceFloat(
        form: LatLng,
        to: LatLng
    ): Float {
        return FloatArray(1).apply {
            Location.distanceBetween(
                form.latitude,
                form.longitude,
                to.latitude,
                to.longitude,
                this
            )
        }[0]
    }
}