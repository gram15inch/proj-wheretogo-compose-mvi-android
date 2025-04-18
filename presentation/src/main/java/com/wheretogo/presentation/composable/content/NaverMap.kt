package com.wheretogo.presentation.composable.content

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.util.FusedLocationSource
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.SEARCH_MARKER
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.toCameraState
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toNaver

@Composable
fun NaverMap(
    modifier: Modifier = Modifier,
    mapOverlayGroup: Collection<MapOverlay> = emptyList(),
    contentPadding: ContentPadding = ContentPadding(),
    cameraState: CameraState = CameraState(),
    onMapAsync: (NaverMap) -> Unit = {},
    onCameraUpdate: (CameraState) -> Unit = {},
    onMapClickListener: (LatLng) -> Unit = {},
    onCourseMarkerClick: (MapOverlay.MarkerContainer) -> Unit = {},
    onCheckPointMarkerClick: (MapOverlay.MarkerContainer) -> Unit = {},
    onOverlayRenderComplete: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView: MapView? by remember { mutableStateOf(null) }
    var isMoving by remember { mutableStateOf(false) }
    //맵 초기화
    LaunchedEffect(Unit) {
        mapView = MapView(context).apply {
            getMapAsync { naverMap ->
                naverMap.apply {
                    onMapAsync(this)
                    naverMap.setUiSetting(context)
                 addOnCameraIdleListener {
                     if(cameraState.updateSource==CameraUpdateSource.USER)
                        onCameraUpdate(naverMap.toCameraState())
                   }

                    setOnMapClickListener { _, latlng ->
                        onMapClickListener(latlng.toDomainLatLng())
                    }
                }
            }
        }

    }

    mapView?.let { syncMapView ->
        DisposableEffect(Unit) {
            val lifecycleObserver =
                LifecycleEventObserver { _, event ->
                    syncMapView.syncLifecycle(event)()
                }
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
            onDispose { lifecycleOwner.lifecycle.removeObserver(lifecycleObserver) }
        }
    }

    //맵 업데이트
    mapView?.getMapAsync { naverMap ->
        naverMap.contentPaddingUpdate(density, contentPadding)
        naverMap.overlayUpdate(
            overlayGroup = mapOverlayGroup,
            onCourseMarkerClick = onCourseMarkerClick,
            onCheckPointMarkerClick = onCheckPointMarkerClick,
            onOverlayRenderComplete = onOverlayRenderComplete
        )

        if(!isMoving && cameraState.updateSource != CameraUpdateSource.USER) {
            isMoving = true
            naverMap.cameraMove(cameraState){
                isMoving = false
            }
        }
    }
    mapView?.apply {
        AndroidView(modifier = modifier, factory = { this })
    }
}


private fun NaverMap.setUiSetting(context: Context) {
    val naverMap = this
    naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
    naverMap.locationSource = context.getMyLocationSource()
    uiSettings.apply {
        isLogoClickEnabled = false
        isLocationButtonEnabled = true
        isZoomControlEnabled = false
        naverMap.minZoom = 8.0
    }
}

private fun NaverMap.contentPaddingUpdate(density: Density, contentPadding: ContentPadding) {
    val naverMap = this
    density.run {
        naverMap.setContentPadding(
            contentPadding.start.toPx().toInt(),
            contentPadding.top.toPx().toInt(),
            contentPadding.end.toPx().toInt(),
            contentPadding.bottom.toPx().toInt()
        )
    }
}

private fun NaverMap.overlayUpdate(
    overlayGroup: Collection<MapOverlay>,
    onCourseMarkerClick: (MapOverlay.MarkerContainer) -> Unit = {},
    onCheckPointMarkerClick: (MapOverlay.MarkerContainer) -> Unit = {},
    onOverlayRenderComplete: (Boolean) -> Unit = {}
) {
    val naverMap = this@overlayUpdate
    var isRendered = overlayGroup.isEmpty()

    overlayGroup.forEachIndexed { i, overlay ->
        when (overlay) {
            is MapOverlay.MarkerContainer -> {
                overlay.marker.map = naverMap
                overlay.marker.setOnClickListener {
                    when (overlay.type) {
                        MarkerType.SPOT -> {
                            onCourseMarkerClick(overlay)
                        }

                        MarkerType.CHECKPOINT -> {
                            if(overlay.id!= SEARCH_MARKER)
                                onCheckPointMarkerClick(overlay)
                        }
                    }
                    true
                }
            }

            is MapOverlay.PathContainer -> {
                overlay.path.map = naverMap
            }
        }
        if (i == overlayGroup.size - 1)
            isRendered = true
    }

    if (isRendered)
        onOverlayRenderComplete(true)
}

private fun NaverMap.cameraMove(cameraState: CameraState, moved:()->Unit) {
    val naverMap = this
    cameraState.apply {
        when (updateSource) {
            CameraUpdateSource.APP_EASING -> {
                naverMap.moveCamera(
                    CameraUpdate.scrollAndZoomTo(latLng.toNaver(), zoom)
                        .animate(CameraAnimation.Easing)
                        .finishCallback { moved() }
                )
            }
            CameraUpdateSource.APP_LINEAR -> {
                naverMap.moveCamera(
                    CameraUpdate.scrollAndZoomTo(latLng.toNaver(), zoom)
                        .animate(CameraAnimation.Linear)
                        .finishCallback { moved() }
                )
            }

            else -> {}
        }
    }
}

private fun Context.getMyLocationSource(): FusedLocationSource {
    return FusedLocationSource(this as ComponentActivity, 1000)
}

private fun MapView.syncLifecycle(event: Lifecycle.Event): () -> Unit {
    return {
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate(Bundle())
            Lifecycle.Event.ON_START -> onStart()
            Lifecycle.Event.ON_RESUME -> { onResume() }
            Lifecycle.Event.ON_PAUSE -> { onPause() }
            Lifecycle.Event.ON_STOP -> onStop()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            Lifecycle.Event.ON_ANY -> {}
        }
    }
}

