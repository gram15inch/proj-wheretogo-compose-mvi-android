package com.wheretogo.presentation.composable.content

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
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
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.MoveAnimation
import com.wheretogo.presentation.feature.geo.FollowLocationSource
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.feature.naver.placeCurrentLocation
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.AppCluster
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.AppPath
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.NaverMapState
import com.wheretogo.presentation.theme.Green50
import com.wheretogo.presentation.toCameraState
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toNaver
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference

@Composable
fun NaverMapSheet(
    modifier: Modifier = Modifier,
    state: NaverMapState,
    overlayGroup: List<MapOverlay> = emptyList(),
    fingerPrint: Int = 0,
    contentPadding: ContentPadding = ContentPadding(),
    onMapAsync: (NaverMap) -> Unit = {},
    onCameraUpdate: (CameraState) -> Unit = {},
    onMapClick: (LatLng) -> Unit = {},
    onMarkerClick: (MarkerInfo) -> Unit = {},
    onOverlayRenderComplete: (Boolean) -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView: MapView? by remember { mutableStateOf(null) }
    var isMoving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // 지도 기본 주소
    val initLatlng = LatLng(latitude = 37.566914081334204, longitude = 126.97838809999871)

    //맵 초기화
    LaunchedEffect(Unit) {
        mapView = MapView(context).apply {
            getMapAsync { naverMap ->
                naverMap.apply {
                    onMapAsync(naverMap)
                    naverMap.setUiSetting(state.isZoomControl)
                    naverMap.locationSetting(context)
                    addOnCameraIdleListener {
                        val cameraSate= naverMap.toCameraState()
                        if (state.latestCameraState.updateSource == CameraUpdateSource.USER
                            && cameraSate.latLng != initLatlng
                        ) onCameraUpdate(cameraSate)
                    }

                    setOnMapClickListener { _, latlng ->
                        onMapClick(latlng.toDomainLatLng())
                    }
                }
            }
        }

    }

    if (isPreview)
        Box(
            modifier
                .fillMaxSize()
                .background(Green50)
        )
    else {
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
        LaunchedEffect(contentPadding) {
            mapView?.getMapAsync { naverMap ->
                naverMap.contentPaddingUpdate(density, contentPadding)
            }
        }
        LaunchedEffect(fingerPrint) {
            mapView?.getMapAsync { naverMap ->
                naverMap.overlayUpdate(
                    overlayGroup = overlayGroup,
                    onMarkerClick = onMarkerClick,
                    onOverlayRenderComplete = onOverlayRenderComplete
                )
            }
        }

        // 카메라 이동 요청
        LaunchedEffect(state.requestCameraState) {
            val camera = state.requestCameraState
            if (camera.isMyLocation) {
                mapView?.getMapAsync {
                    coroutineScope.launch { it.placeCurrentLocation(context) }
                }
            } else if(!isMoving && camera.latLng != LatLng()) {
                mapView?.getMapAsync { naverMap ->

                    isMoving = true
                    naverMap.setGesture(false)
                    naverMap.cameraMove(camera) {
                        isMoving = false
                        naverMap.setGesture(true)
                    }
                }
            }
        }

        mapView?.apply {
            AndroidView(modifier = modifier, factory = { this })
        }
    }

}

private fun NaverMap.locationSetting(context: Context) {
    locationTrackingMode = LocationTrackingMode.NoFollow
    val map = WeakReference(this)
    locationSource = FollowLocationSource(context, {
        map.get()?.locationTrackingMode ?: LocationTrackingMode.NoFollow
    })
}

private fun NaverMap.setGesture(isEnable: Boolean) {
    uiSettings.isStopGesturesEnabled = isEnable
    uiSettings.isScrollGesturesEnabled = isEnable
    uiSettings.isZoomGesturesEnabled = isEnable
    uiSettings.isTiltGesturesEnabled = isEnable
    uiSettings.isRotateGesturesEnabled = isEnable
}

private fun NaverMap.setUiSetting(isZoomControl: Boolean) {
    uiSettings.apply {
        isLogoClickEnabled = false
        isLocationButtonEnabled = true
        isZoomControlEnabled = isZoomControl
    }
    minZoom = 8.0
    setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, false)
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
    overlayGroup: List<MapOverlay>,
    onMarkerClick: (MarkerInfo) -> Unit = {},
    onOverlayRenderComplete: (Boolean) -> Unit = {}
) {
    val naverMap = this@overlayUpdate
    var isRendered = overlayGroup.isEmpty()

    overlayGroup.forEachIndexed { i, overlay ->
        when (overlay) {
            is AppMarker -> {
                val marker = overlay.coreMarker

                if (marker != null) {
                    marker.map = naverMap
                    marker.setOnClickListener {
                        onMarkerClick(overlay.markerInfo)
                        true
                    }
                }

            }

            is AppPath -> {
                overlay.corePathOverlay?.map = naverMap
            }
            is AppCluster -> {
                overlay.clusterHolder.cluster?.map = naverMap
            }
        }
        if (i == overlayGroup.size - 1)
            isRendered = true
    }

    if (isRendered)
        onOverlayRenderComplete(true)
}

private fun NaverMap.cameraMove(cameraState: CameraState, moved: () -> Unit) {
    val naverMap = this
    cameraState.apply {
        when (moveAnimation) {
            MoveAnimation.APP_EASING -> {
                naverMap.moveCamera(
                    CameraUpdate.scrollAndZoomTo(latLng.toNaver(), zoom)
                        .animate(CameraAnimation.Easing)
                        .finishCallback { moved() }
                        .cancelCallback { moved() }
                )
            }

            MoveAnimation.APP_LINEAR -> {
                naverMap.moveCamera(
                    CameraUpdate.scrollAndZoomTo(latLng.toNaver(), zoom)
                        .animate(CameraAnimation.Linear)
                        .finishCallback { moved() }
                        .cancelCallback { moved() }
                )
            }
        }
    }
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