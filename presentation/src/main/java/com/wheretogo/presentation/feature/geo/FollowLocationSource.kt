package com.wheretogo.presentation.feature.geo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.LocationSource
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.util.FusedLocationSource
import com.wheretogo.presentation.AppPermission
import com.wheretogo.presentation.feature.requestPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.lang.ref.WeakReference

class FollowLocationSource(val context: Context, val modeCheck: () -> LocationTrackingMode) :
    LocationSource {
    private var job: Job? = null
    private val weakContext = WeakReference(context)
    private val fusedLocationSource =
        weakContext.get()?.run { FusedLocationSource(this as Activity, 1000) }

    @SuppressLint("MissingPermission")
    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        if (modeCheck() == LocationTrackingMode.Follow) {
            job = CoroutineScope(Dispatchers.Main).launch {
                runCatching {
                    weakContext.get()?.let {
                        listener.onLocationChanged(getLocationWithPermission(it))
                    }
                }.onFailure {
                    fusedLocationSource?.activate(listener)
                }
            }
        } else {
            fusedLocationSource?.activate(listener)
        }
    }

    override fun deactivate() {
        job?.cancel()
        job = null
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocationWithPermission(context: Context): Location? {
        if (!requestPermission(context, AppPermission.LOCATION))
            return null
        return withTimeout(10000){
            LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
        }

    }
}