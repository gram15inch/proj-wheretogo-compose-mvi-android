package com.wheretogo.presentation.composable.content

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.wheretogo.domain.CHECKPOINT_TYPE
import com.wheretogo.domain.COURSE_TYPE
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Viewport
import com.wheretogo.presentation.feature.map.distanceTo
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.toDomainLatLng
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@Composable
fun NaverMap(
    modifier: Modifier,
    overlayMap: Set<MapOverlay>,
    onMapAsync: (NaverMap) -> Unit,
    onLocationMove: (LatLng) -> Unit,
    onCameraMove: (LatLng, Viewport) -> Unit,
    onCourseMarkerClick: (Overlay) -> Unit,
    onCheckPointMarkerClick: (Overlay) -> Unit,
    onOverlayRenderComplete: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var latestCameraPosition by remember { mutableStateOf(LatLng()) }
    var latestLocation by remember { mutableStateOf(LatLng()) }
    val mapView = remember {
        MapView(context).apply {
            getMapAsync { naverMap ->
                naverMap.apply {
                    onMapAsync(this)

                    uiSettings.apply {
                        isLocationButtonEnabled = true
                        isZoomControlEnabled = false
                        naverMap.minZoom = 11.0
                        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
                    }

                    addOnLocationChangeListener { location ->
                        LatLng(location.latitude, location.latitude).let { latlng ->
                            if (latestLocation.distanceTo(latlng) >= 1) {
                                onLocationMove(latlng)
                                latestLocation = latlng
                            }
                        }
                    }

                    addOnCameraIdleListener {
                        if (latestCameraPosition.isNotEqual(cameraPosition.target.toDomainLatLng())) {
                            contentRegion.apply {
                                onCameraMove(
                                    cameraPosition.target.toDomainLatLng(), Viewport(
                                        this[0].latitude,
                                        this[3].latitude,
                                        this[0].longitude,
                                        this[3].longitude
                                    )
                                )
                            }
                            latestCameraPosition = this.cameraPosition.target.toDomainLatLng()
                        }
                    }
                }
            }
        }
    }
    val lifecycleObserver = remember {
        LifecycleEventObserver { source, event ->
            coroutineScope.launch {
                mapView.syncLifecycle(source, event)()
            }
        }
    }
    DisposableEffect(true) {
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }
    mapView.getMapAsync { map ->
        coroutineScope.launch {
            var isRendered = false
            overlayMap.map {
                async {
                    val path = async {
                        it.pathOverlay.apply {
                            if (coords.isNotEmpty()) {
                                this.map = this.map ?: map
                            }
                        }
                    }

                    val marker = async {
                        it.marker.apply {
                            if (it.marker.position.latitude.toString() != "NaN") {
                                this.map = this.map ?: run {
                                    if (!isRendered)
                                        isRendered = true
                                    map
                                }
                                if (this.onClickListener == null) {

                                    this.setOnClickListener { overlay ->
                                        when (it.type) {
                                            CHECKPOINT_TYPE -> {
                                                onCheckPointMarkerClick(overlay)
                                            }

                                            COURSE_TYPE -> {
                                                onCourseMarkerClick(overlay)
                                            }
                                        }
                                        false
                                    }
                                }
                            }
                        }
                    }
                    path.await()
                    marker.await()
                }
            }.awaitAll()
            if (isRendered)
                onOverlayRenderComplete(true)
        }
    }
    AndroidView(modifier = modifier, factory = { mapView })
}


fun LatLng.isNotEqual(latlng: LatLng): Boolean {
    val precision = 1000.0

    return !(((latitude * precision).toInt() == (latlng.latitude * precision).toInt()) &&
            ((longitude * precision).toInt() == (latlng.longitude * precision).toInt()))
}

private fun Context.getMyLocationSource(): FusedLocationSource {
    return FusedLocationSource(this as ComponentActivity, 1000)
}

private fun MapView.syncLifecycle(source: LifecycleOwner, event: Lifecycle.Event): () -> Unit {
    return {
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate(Bundle())
            Lifecycle.Event.ON_START -> onStart()
            Lifecycle.Event.ON_RESUME -> {
                onResume()
                getMapAsync { naverMap ->
                    context.getMyLocationSource().apply {
                        naverMap.locationSource = this
                    }.let {
                        naverMap.locationTrackingMode = LocationTrackingMode.Follow
                    }
                }
            }

            Lifecycle.Event.ON_PAUSE -> onPause()
            Lifecycle.Event.ON_STOP -> onStop()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            Lifecycle.Event.ON_ANY -> {}
        }
    }
}

