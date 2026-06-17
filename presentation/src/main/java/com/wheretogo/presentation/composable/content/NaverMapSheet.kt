 package com.wheretogo.presentation.composable.content

import android.content.Context
import android.os.Bundle
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
fun NaverMapSheet(
    modifier: Modifier = Modifier,
    state: NaverMapState,
    event: SharedFlow<MapEvent>? = null,
    overlayGroup: List<MapOverlay> = emptyList(),
    fingerPrint: StateFlow<Int>? = null,
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
    val coroutineScope = rememberCoroutineScope()

    // SDK 기본 주소
    val initLatlng = LatLng(latitude = 37.566914081334204, longitude = 126.97838809999871)
    val initLatlng2 = LatLng(latitude = 37.56827472881153, longitude = 126.97838809999871)
    val initLatlng3 = LatLng(latitude = 37.56745834330747, longitude = 126.97799900088177)

    var mapView: MapView? by remember { mutableStateOf(null) }
    var isMoving by remember { mutableStateOf<Boolean>(false) }
    var initPadding by remember { mutableStateOf(contentPadding.bottom) }
    var listener : NaverMap.OnCameraIdleListener? by remember { mutableStateOf(null) }

    fun NaverMap.addCameraListener(){
        if(listener==null)
            NaverMap.OnCameraIdleListener {
                val cameraSate = toCameraState()
                if (!listOf(initLatlng,initLatlng2,initLatlng3).contains(cameraSate.latLng)) {
                    onCameraUpdate(cameraSate)
                }
            }.let {
                addOnCameraIdleListener(it)
                listener = it
            }
    }

    fun NaverMap.removeCameraListener(){
        listener?.let {
            removeOnCameraIdleListener(it)
            listener = null
        }
    }

    if (isPreview) {
        Box(
            modifier
                .fillMaxSize()
                .background(Palette.Green50)
        )
    } else {
        // 초기 설정
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
        val mapBottomPadding by animateDpAsState(
            targetValue = contentPadding.bottom,
            animationSpec = tween(durationMillis = 300)
        )

        //맵 초기화
        LaunchedEffect(Unit) {
            mapView = MapView(context).apply {
                getMapAsync { naverMap ->
                    naverMap.apply {
                        onMapAsync(naverMap)
                        naverMap.setUiSetting(state.isZoomControl)
                        naverMap.locationSetting(context)
                        naverMap.addCameraListener()

                        setOnMapClickListener { _, latlng ->
                            onMapClick(latlng.toDomainLatLng())
                        }
                    }
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
                        mapView?.getMapAsync { naverMap ->
                            when {
                                isMoving -> {}
                                camera.isMyLocation -> coroutineScope.launch {
                                    val latlng = getLastLatLng(context) ?: NamSan
                                    naverMap.cameraPosition = CameraPosition(latlng, camera.zoom)
                                }

                                camera.moveAnimation == MoveAnimation.APP_JUMP -> {
                                    naverMap.cameraPosition =
                                        CameraPosition(camera.latLng.toNaver(), camera.zoom)
                                }

                                camera.latLng != LatLng() -> {
                                    isMoving = true
                                    naverMap.isCameraIdlePending = true
                                    naverMap.setGesture(false)
                                    naverMap.cameraMove(camera) {
                                        coroutineScope
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

        // 패딩 업데이트
        LaunchedEffect(mapBottomPadding) {
            mapView?.getMapAsync { naverMap ->
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

        // 오버레이 업데이트
        LaunchedEffect(Unit) {
            fingerPrint?.collect {
                mapView?.getMapAsync { naverMap ->
                    naverMap.overlayUpdate(
                        overlayGroup = overlayGroup,
                        onMarkerClick = onMarkerClick,
                        onOverlayRenderComplete = onOverlayRenderComplete
                    )
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
    minZoom = ZOOM.COUNTRY.level
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

private fun NaverMap.cameraMove(option: CameraOption,  moved: () -> Unit) {
    val naverMap = this
    option.latLng.let { latLng ->
        naverMap.moveCamera(
            CameraUpdate.scrollAndZoomTo(latLng.toNaver(),option.zoom)
                .animate(option.moveAnimation.toCameraAnimation())
                .finishCallback { moved() }
                .cancelCallback { moved() }
        )
    }
}

fun MoveAnimation.toCameraAnimation(): CameraAnimation{
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
            Lifecycle.Event.ON_RESUME -> { onResume() }
            Lifecycle.Event.ON_PAUSE -> { onPause() }
            Lifecycle.Event.ON_STOP -> onStop()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            Lifecycle.Event.ON_ANY -> {}
        }
    }
}