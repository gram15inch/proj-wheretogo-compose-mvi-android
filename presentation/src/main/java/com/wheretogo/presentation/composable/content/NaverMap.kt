package com.wheretogo.presentation.composable.content

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport
import com.wheretogo.presentation.model.toDomainLatLng
import com.wheretogo.presentation.model.toNaver
import kotlinx.coroutines.launch

@Composable
fun NaverMap(
    data: State<Set<Journey>>,
    onMapAsync: (NaverMap) -> Unit,
    onLocationMove: (LatLng) -> Unit,
    onCameraMove: (LatLng) -> Unit,
    onViewportChange: (LatLng, Viewport) -> Unit,
    onMarkerClick: (Overlay) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val mapView = remember {
        MapView(context).apply {
            getMapAsync { naverMap ->
                onMapAsync(naverMap)
                naverMap.uiSettings.apply {
                    isLocationButtonEnabled = true
                    isZoomControlEnabled = false
                    naverMap.minZoom = 11.0
                    naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
                }

                naverMap.setListener(onLocationMove, onCameraMove, onViewportChange)

            }
        }
    }
    val lifecycleObserver = remember {
        LifecycleEventObserver { source, event ->

            coroutineScope.launch {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> {
                        mapView.onResume()
                        mapView.getMapAsync { naverMap ->
                            context.getMyLocationSource().apply {
                                naverMap.locationSource = this
                            }.let {
                                naverMap.locationTrackingMode = LocationTrackingMode.Follow
                            }
                        }
                    }

                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    Lifecycle.Event.ON_ANY -> {}
                }
            }
        }
    }

    DisposableEffect(true) {
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    mapView.getMapAsync { naverMap ->
        if (data.value.isNotEmpty()) {
            data.value.forEach { item ->
                val naverPoints = item.points.toNaver()
                Marker().apply {
                    position = naverPoints[0] // todo 인덱스 에러 처리
                    map = naverMap
                    tag = item.code
                    this.setOnClickListener { overlay ->
                        onMarkerClick(overlay)

                        true
                    }
                }
                PathOverlay().apply {
                    coords = naverPoints
                    map = naverMap
                }
            }
        }
    }
    AndroidView(factory = { mapView })
}


private fun NaverMap.setListener(
    onMoveLocation: (LatLng) -> Unit,
    onMoveCamera: (LatLng) -> Unit,
    onViewPortChange: (LatLng, Viewport) -> Unit
) {

    addOnLocationChangeListener { location ->
        onMoveLocation(LatLng(location.latitude, location.longitude))
    }

    addOnCameraIdleListener {
        onMoveCamera(this.cameraPosition.target.toDomainLatLng())
        contentRegion.apply {
            onViewPortChange(
                cameraPosition.target.toDomainLatLng(),
                Viewport(
                    this[0].latitude,
                    this[3].latitude,
                    this[0].longitude,
                    this[3].longitude
                )
            )
        }
    }
}

private fun Context.getMyLocationSource(): FusedLocationSource {
    return FusedLocationSource(this as ComponentActivity, 1000)
}