package com.wheretogo.presentation.feature.geo

import android.location.Location
import com.naver.maps.geometry.LatLngBounds
import com.wheretogo.domain.feature.LocationService
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.util.Viewport
import com.wheretogo.presentation.toNaver
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

    override fun isContainByViewPort(vp: Viewport, target: LatLng): Boolean {
        val rectBounds = LatLngBounds(
            vp.southWest.toNaver(),
            vp.northEast.toNaver()
        )
        return rectBounds.contains(target.toNaver())
    }
}