package com.wheretogo.presentation.feature

import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.wheretogo.domain.model.LatLng

fun NaverMap.setCamera(
    camera: LatLng,
    zoom: Double = 11.0,
    tilt: Double = 0.0,
    bearing: Double = 0.0
) {
    cameraPosition = CameraPosition(
        com.naver.maps.geometry.LatLng(
            camera.latitude,
            camera.longitude
        ), zoom, tilt, bearing
    )
}

fun NaverMap.moveCamera(camera: LatLng, zoom: Double = 11.0) {
    var cameraUpdate = CameraUpdate.scrollAndZoomTo(
        com.naver.maps.geometry.LatLng(
            camera.latitude,
            camera.longitude
        ), zoom
    )
    cameraUpdate = cameraUpdate.animate(CameraAnimation.Easing)
    moveCamera(cameraUpdate)
}
