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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.wheretogo.domain.OverlayType
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Viewport
import com.wheretogo.presentation.CameraStatus
import com.wheretogo.presentation.MarkerIconType
import com.wheretogo.presentation.feature.geo.distanceTo
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toNaver
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@Composable
fun NaverMap(
    modifier: Modifier = Modifier,
    overlayMap: Set<MapOverlay> = emptySet(),
    contentPadding: ContentPadding = ContentPadding(),
    cameraState: CameraState = CameraState(),
    onMapAsync: (NaverMap) -> Unit = {},
    onLocationMove: (LatLng) -> Unit = {},
    onCameraMove: (CameraState) -> Unit = {},
    onMapClickListener: (LatLng) -> Unit = {},
    onCourseMarkerClick: (Overlay) -> Unit = {},
    onCheckPointMarkerClick: (Overlay) -> Unit = {},
    onOverlayRenderComplete: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var latestLocation by remember { mutableStateOf(LatLng()) }
    val mapView = remember {
        MapView(context).apply {
            getMapAsync { naverMap ->
                naverMap.apply {
                    onMapAsync(this)

                    uiSettings.apply {
                        isLocationButtonEnabled = true
                        isZoomControlEnabled = false
                        naverMap.minZoom = 8.0
                        logoGravity = 2
                        setLogoMargin(20,100,0,0)
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
                        contentRegion.apply {
                            onCameraMove(
                                CameraState(
                                    latLng = cameraPosition.target.toDomainLatLng(),
                                    zoom = cameraPosition.zoom,
                                    viewport = Viewport(
                                        this[0].latitude,
                                        this[3].latitude,
                                        this[0].longitude,
                                        this[3].longitude
                                    )
                                )
                            )
                        }
                    }

                    setOnMapClickListener { _, latlng ->
                        onMapClickListener(latlng.toDomainLatLng())
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
    mapView.getMapAsync { naverMap ->
        coroutineScope.launch {
            var isRendered = overlayMap.isEmpty()
            contentPadding.apply {
                with(density) {
                    naverMap.setContentPadding(
                        start.toPx().toInt(),
                        top.toPx().toInt(),
                        end.toPx().toInt(),
                        bottom.toPx().toInt()
                    )
                }
            }
            overlayMap.map { overlay ->
                async {
                    val marker = async {
                        overlay.markerGroup.forEach {
                            if (it.position.latitude.toString() != "NaN") {
                                isRendered = true
                                it.map = naverMap
                                when (overlay.overlayType) {
                                    OverlayType.CHECKPOINT -> {
                                        if(overlay.iconType!= MarkerIconType.DEFAULT)
                                            it.icon =  OverlayImage.fromResource(overlay.iconType.res)
                                        it.setOnClickListener {
                                            onCheckPointMarkerClick(it)
                                            true
                                        }
                                    }

                                    OverlayType.COURSE -> {
                                        it.icon =  OverlayImage.fromResource(overlay.iconType.res)
                                        it.setOnClickListener {
                                            onCourseMarkerClick(it)
                                            true
                                        }

                                    }
                                    else->{}
                                }
                            }
                        }
                    }

                    val path = async {
                        overlay.path?.apply {
                            if (coords.isNotEmpty()) {
                                isRendered = true
                                this.map = naverMap
                            }
                        }
                    }
                    marker.await()
                    path.await()
                }
            }.awaitAll().apply {
                if (isRendered)
                    onOverlayRenderComplete(true)
            }

            cameraState.apply {
                when (status) {
                    CameraStatus.TRACK -> {
                        naverMap.moveCamera(
                            CameraUpdate.zoomTo(cameraState.zoom).animate(
                                CameraAnimation.Easing
                            )
                        )
                        naverMap.moveCamera(
                            CameraUpdate.scrollTo(cameraState.latLng.toNaver()).animate(
                                CameraAnimation.Easing
                            )
                        )
                    }

                    CameraStatus.INIT -> {
                        naverMap.cameraPosition =
                            CameraPosition(cameraState.latLng.toNaver(), cameraState.zoom)
                    }

                    else -> {}
                }
            }
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
                        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
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

