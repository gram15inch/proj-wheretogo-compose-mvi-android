package com.wheretogo.presentation.composable.content

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.wheretogo.domain.ZOOM
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.map.CameraState
import com.wheretogo.domain.model.map.MarkerInfo
import com.wheretogo.domain.model.map.MoveAnimation
import com.wheretogo.presentation.NamSan
import com.wheretogo.presentation.feature.geo.FollowLocationSource
import com.wheretogo.presentation.feature.naver.getLastLatLng
import com.wheretogo.presentation.model.AppCluster
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.AppPath
import com.wheretogo.presentation.model.CameraOption
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.NaverMapStyle
import com.wheretogo.presentation.state.NaverMapState
import com.wheretogo.presentation.theme.Palette
import com.wheretogo.presentation.toCameraState
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toNaver
import com.wheretogo.presentation.viewmodel.MapEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

@Composable
fun rememberMapViewWithLifecycle(
): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply { id = View.generateViewId() }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(mapView) {
        val lifecycleObserver =
            LifecycleEventObserver { _, event ->
                mapView.syncLifecycle(event)()
            }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }
    return mapView
}

@Composable
fun NaverMapSheet(
    mapView: MapView,
    state: NaverMapState,
    modifier: Modifier = Modifier,
    style: NaverMapStyle = NaverMapStyle.Basic,
    overlayGroup: List<MapOverlay> = emptyList(),
    fingerPrint: StateFlow<Int>? = null,
    event: SharedFlow<MapEvent>? = null,
    contentPadding: ContentPadding = ContentPadding(),
    onMapAsync: (NaverMap) -> Unit = {},
    onMapClick: (LatLng) -> Unit = {},
    onMarkerClick: (MarkerInfo) -> Unit = {},
    onCameraUpdate: (CameraState) -> Unit = {},
    onOverlayRenderComplete: (Boolean) -> Unit = {},
) {
    // SDK 기본 주소
    val initLatlng = LatLng(latitude = 37.566914081334204, longitude = 126.97838809999871)
    val initLatlng2 = LatLng(latitude = 37.56827472881153, longitude = 126.97838809999871)
    val initLatlng3 = LatLng(latitude = 37.56745834330747, longitude = 126.97799900088177)

    var cameraIdleListener: NaverMap.OnCameraIdleListener? by remember { mutableStateOf(null) }

    fun NaverMap.addCameraListener() {
        if (cameraIdleListener == null)
            NaverMap.OnCameraIdleListener {
                val cameraSate = toCameraState()
                if (!listOf(initLatlng, initLatlng2, initLatlng3).contains(cameraSate.latLng)) {
                    onCameraUpdate(cameraSate)
                }
            }.let {
                addOnCameraIdleListener(it)
                cameraIdleListener = it
            }
    }

    fun NaverMap.addMapClickListener() {
        if (onMapClickListener == null)
            NaverMap.OnMapClickListener { _, latlng ->
                onMapClick(latlng.toDomainLatLng())
            }.let {
                onMapClickListener = it
            }
    }

    fun NaverMap.removeCameraListener() {
        cameraIdleListener?.let {
            removeOnCameraIdleListener(it)
            cameraIdleListener = null
        }
    }

    fun NaverMap.removeMapClickListener() {
        onMapClickListener = null
    }

    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Palette.Green50)
        )
    } else {
        val context = LocalContext.current
        val density = LocalDensity.current
        var isMoving by remember { mutableStateOf<Boolean>(false) }
        var initPadding by remember { mutableStateOf(contentPadding.bottom) }

        val coroutineScope = rememberCoroutineScope()

        //맵 초기화
        DisposableEffect(mapView) {
            mapView.getMapAsync { naverMap ->
                onMapAsync(naverMap)
                state.initCamera?.let { option -> naverMap.cameraUpdate(option) }
                naverMap.setMapStyle(style)
                naverMap.locationSetting(context)
                naverMap.addCameraListener()
                naverMap.addMapClickListener()
            }
            onDispose {
                mapView.getMapAsync { naverMap ->
                    naverMap.removeCameraListener()
                    naverMap.removeMapClickListener()
                }
            }
        }

        // 오버레이 업데이트
        LaunchedEffect(Unit) {
            fingerPrint?.collect {
                mapView.getMapAsync { naverMap ->
                    naverMap.overlayUpdate(
                        overlayGroup = overlayGroup,
                        onMarkerClick = onMarkerClick,
                        onOverlayRenderComplete = onOverlayRenderComplete
                    )
                }
            }
        }

        // 패딩 업데이트
        val mapBottomPadding by animateDpAsState(
            targetValue = contentPadding.bottom,
            animationSpec = tween(durationMillis = 300)
        )
        LaunchedEffect(mapBottomPadding) {
            mapView.getMapAsync { naverMap ->
                if (mapBottomPadding != initPadding) {
                    naverMap.removeCameraListener()
                    naverMap.contentPaddingUpdate(
                        density,
                        contentPadding.copy(bottom = mapBottomPadding)
                    )
                } else {
                    naverMap.addCameraListener()
                    naverMap.contentPaddingUpdate(
                        density,
                        contentPadding.copy(bottom = mapBottomPadding)
                    )
                }
            }
        }


        // 맵 이벤트
        LaunchedEffect(Unit) {
            event?.collect { e ->
                when (e) {
                    // 카메라 이동 요청
                    is MapEvent.MoveCamera -> {
                        val camera = e.cameraState
                        mapView.getMapAsync { naverMap ->
                            when {
                                isMoving -> {}
                                camera.isMyLocation -> coroutineScope.launch {
                                    val latlng = getLastLatLng(context) ?: NamSan
                                    naverMap.cameraUpdate(camera.copy(latlng.toDomainLatLng()))
                                }

                                camera.moveAnimation == MoveAnimation.APP_JUMP -> {
                                    naverMap.cameraUpdate(camera)
                                }

                                camera.latLng != LatLng() -> {
                                    isMoving = true
                                    naverMap.isCameraIdlePending = true
                                    naverMap.setGesture(false)
                                    naverMap.cameraMove(camera) {
                                        isMoving = false
                                        naverMap.isCameraIdlePending = false
                                        naverMap.setGesture(true)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AndroidView(modifier = modifier, factory = {
            (mapView.parent as? ViewGroup)?.removeView(mapView)
            mapView
        })
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

private fun NaverMap.cameraMove(option: CameraOption, moved: () -> Unit) {
    val naverMap = this
    option.latLng.let { latLng ->
        naverMap.moveCamera(
            CameraUpdate.scrollAndZoomTo(latLng.toNaver(), option.zoom)
                .animate(option.moveAnimation.toCameraAnimation())
                .finishCallback { moved() }
                .cancelCallback { moved() }
        )
    }
}

private fun NaverMap.cameraUpdate(option: CameraOption) {
    val naverMap = this
    naverMap.cameraPosition = option.toPosition()
}

private fun NaverMap.setMapStyle(style: NaverMapStyle) {
    mapType = style.mapType

    setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, style.buildingEnabled)
    setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, style.transitEnabled)
    setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, style.bicycleEnabled)
    setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, style.trafficEnabled)
    setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, style.cadastralEnabled)
    setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN, style.mountainEnabled)

    symbolScale = style.symbolScale
    symbolPerspectiveRatio = style.symbolPerspectiveRatio

    isIndoorEnabled = style.indoorEnabled
    isNightModeEnabled = style.nightModeEnabled

    uiSettings.apply {
        isScrollGesturesEnabled = style.scrollGesturesEnabled
        isZoomGesturesEnabled = style.zoomGesturesEnabled
        isTiltGesturesEnabled = style.tiltGesturesEnabled
        isRotateGesturesEnabled = style.rotateGesturesEnabled
        isZoomControlEnabled = style.zoomControlEnabled
        isCompassEnabled = style.compassEnabled
        isScaleBarEnabled = style.scaleBarEnabled
        isLocationButtonEnabled = style.locationButtonEnabled
        isLogoClickEnabled = style.logoClickEnabled
    }
}

private fun CameraOption.toPosition(): CameraPosition {
    return CameraPosition(latLng.toNaver(), zoom)
}

fun MoveAnimation.toCameraAnimation(): CameraAnimation {
    return when (this) {
        MoveAnimation.APP_EASING -> CameraAnimation.Easing
        MoveAnimation.APP_LINEAR -> CameraAnimation.Linear
        MoveAnimation.APP_JUMP -> CameraAnimation.Linear
    }
}

private fun MapView.syncLifecycle(event: Lifecycle.Event): () -> Unit {
    return {
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate(Bundle())
            Lifecycle.Event.ON_START -> onStart()
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_PAUSE -> onPause()
            Lifecycle.Event.ON_STOP -> onStop()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            Lifecycle.Event.ON_ANY -> {}
        }
    }
}