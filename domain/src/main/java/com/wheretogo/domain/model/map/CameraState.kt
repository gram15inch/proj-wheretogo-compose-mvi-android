package com.wheretogo.domain.model.map

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.course.GeoBounds
import com.wheretogo.domain.model.course.GeoPoint
import com.wheretogo.domain.model.course.MapCamera
import com.wheretogo.domain.model.util.Viewport

data class CameraState(
    val latLng: LatLng = LatLng(),
    val zoom: Double = 0.0,
    val viewport: Viewport = Viewport()
){
    fun toMapCamera(): MapCamera {
        return  MapCamera(
            zoom = zoom,
            target = GeoPoint(
                lat = latLng.latitude,
                lng = latLng.longitude
            ),
            viewport = GeoBounds(
                swLat = viewport.southWest.latitude,
                swLng = viewport.southWest.longitude,
                neLat = viewport.northEast.latitude,
                neLng = viewport.northEast.longitude
            )
        )
    }
}