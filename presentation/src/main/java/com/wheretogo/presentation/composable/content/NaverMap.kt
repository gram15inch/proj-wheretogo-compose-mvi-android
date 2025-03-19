package com.wheretogo.presentation.composable.content

import android.content.Context
import android.os.Bundle
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
import com.wheretogo.presentation.CameraStatus
import com.wheretogo.presentation.MarkerIconType
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.OverlayTag
import com.wheretogo.presentation.parse
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.toCameraState
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toNaver

@Composable
fun NaverMap(
    modifier: Modifier = Modifier,
    overlayMap: Set<MapOverlay> = emptySet(),
    contentPadding: ContentPadding = ContentPadding(),
    cameraState: CameraState = CameraState(),
    onMapAsync: (NaverMap) -> Unit = {},
    onCameraMove: (CameraState) -> Unit = {},
    onMapClickListener: (LatLng) -> Unit = {},
    onCourseMarkerClick: (Overlay) -> Unit = {},
    onCheckPointMarkerClick: (Overlay) -> Unit = {},
    onOverlayRenderComplete: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView: MapView? by remember { mutableStateOf(null) }

    //맵 초기화
    LaunchedEffect(Unit) {
        mapView = MapView(context).apply {
            getMapAsync { naverMap ->
                naverMap.apply {
                    onMapAsync(this)

                    naverMap.setUiSetting(context)

                    addOnCameraIdleListener {
                        onCameraMove(naverMap.toCameraState())
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
            overlayMap = overlayMap,
            onCheckPointMarkerClick = onCheckPointMarkerClick,
            onCourseMarkerClick = onCourseMarkerClick,
            onOverlayRenderComplete= onOverlayRenderComplete
        )

        naverMap.cameraUpdate(cameraState)
    }
    mapView?.apply {
        AndroidView(modifier = modifier, factory = { this })
    }
}



private fun NaverMap.setUiSetting(context:Context){
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

private fun NaverMap.contentPaddingUpdate(density:Density,contentPadding: ContentPadding){
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
    overlayMap: Set<MapOverlay>,
    onCheckPointMarkerClick: (Overlay) -> Unit = {},
    onCourseMarkerClick: (Overlay) -> Unit = {},
    onOverlayRenderComplete: (Boolean) -> Unit = {}
) {
    val naverMap = this@overlayUpdate
    var isRendered = overlayMap.isEmpty()
    overlayMap.map { overlay ->
        overlay.markerGroup.forEach {
            if (it.position.latitude.toString() != "NaN") {
                isRendered = true
                it.map = naverMap
                val overlayTag = OverlayTag.parse(it.tag as String)?.iconType?:MarkerIconType.DEFAULT
                when (overlay.overlayType) {
                    OverlayType.CHECKPOINT -> {
                        if(overlayTag != MarkerIconType.DEFAULT)
                            it.icon = OverlayImage.fromResource(overlayTag.res)
                        it.setOnClickListener {
                            onCheckPointMarkerClick(it)
                            true
                        }
                    }

                    OverlayType.COURSE -> {
                        it.icon = OverlayImage.fromResource(overlayTag.res)
                        it.setOnClickListener {
                            onCourseMarkerClick(it)
                            true
                        }

                    }

                    else -> {}
                }
            }
        }

        overlay.path?.apply {
            if (coords.isNotEmpty()) {
                isRendered = true
                this.map = naverMap
            }

        }

        if (isRendered)
            onOverlayRenderComplete(true)
    }
}

private fun NaverMap.cameraUpdate(cameraState: CameraState){
    val naverMap = this
    cameraState.apply {
        when (status) {
            CameraStatus.TRACK -> {
                naverMap.moveCamera(
                    CameraUpdate.scrollAndZoomTo(latLng.toNaver(),zoom)
                        .animate(CameraAnimation.Easing)
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

