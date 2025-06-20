package com.wheretogo.presentation.feature.geo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.naver.maps.map.LocationSource
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.util.FusedLocationSource
import com.wheretogo.presentation.feature.getLocationWithPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
}