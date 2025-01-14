package com.wheretogo.presentation.feature.naver

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.wheretogo.domain.model.map.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun NaverMap.rotateCamera(latLng: LatLng){
    if (cameraPosition.bearing != 0.0 || cameraPosition.tilt != 0.0)
        setCamera(latLng, 13.0, 0.0, 0.0)
    else
        setCamera(latLng, 14.0, 90.0, -25.0)
}

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

@SuppressLint("MissingPermission")
suspend fun NaverMap.setCurrentLocation(context: Context, zoom: Double = 13.0) {
    val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    val location = suspendCancellableCoroutine { continuation ->
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            continuation.resume(location)
        }.addOnFailureListener {
            continuation.resume(null)
        }
    }
    location?.let {
        cameraPosition =
            CameraPosition(com.naver.maps.geometry.LatLng(it.latitude, it.longitude), zoom)
    }
    return
}
