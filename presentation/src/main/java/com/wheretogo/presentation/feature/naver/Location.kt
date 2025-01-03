package com.wheretogo.presentation.feature.naver

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.NaverMap
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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