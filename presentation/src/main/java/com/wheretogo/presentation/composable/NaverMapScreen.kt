package com.wheretogo.presentation.composable

import android.accounts.Account
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.skt.Tmap.TMapTapi
import com.valentinilk.shimmer.shimmer
import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.c2
import com.wheretogo.presentation.model.toDomainLatLng
import com.wheretogo.presentation.model.toNaver
import kotlinx.coroutines.launch

@Composable
fun NaverMapScreen(
    data: State<List<Journey>>,
    camera: LatLng,
    onViewPortChange: (LatLng, Viewport) -> Unit,
    onMarkerClick: (Overlay) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.wrapContentSize(), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (data.value.isEmpty())
                ShimmeringPlaceholder()
            NaverMapComposable(data, camera, onViewPortChange, onMarkerClick)
        }
        Column(
            Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier
                    .clickable {
                        context.searchNaverMap(c2)
                    },
                text = "네이버지도에서 찾기", fontSize = 20.sp
            )
            Text(
                modifier = Modifier
                    .clickable {
                        context.searchTMap(c2)
                    },
                text = "티맵에서 찾기", fontSize = 20.sp
            )
        }
    }
}

@Composable
fun ShimmeringPlaceholder() {
    Row(
        modifier = Modifier
            .shimmer()
            .fillMaxWidth()
            .height(400.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.LightGray),
        )
    }
}

@Composable
fun NaverMapComposable(
    data: State<List<Journey>>,
    camera: LatLng,
    onViewPortChange: (LatLng, Viewport) -> Unit,
    onMarkerClick: (Overlay) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val mapView = remember {
        MapView(context).apply {
            getMapAsync { naverMap ->
                naverMap.uiSettings.apply {
                    isLocationButtonEnabled = true
                    isZoomControlEnabled = false
                    naverMap.minZoom = 11.0
                }

                naverMap.initLocation()

                naverMap.addOnCameraIdleListener {
                    naverMap.contentRegion.apply {
                        onViewPortChange(
                            naverMap.cameraPosition.target.toDomainLatLng(),
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
        if(camera.latitude!=0.0){
            naverMap.setCamera(camera,12.0)
        }
        if (data.value.isNotEmpty()) {
            data.value.forEach { item ->
                val naverPoints = item.points.toNaver()
                Marker().apply {
                    position = naverPoints[0] // todo 인덱스 에러 처리
                    map = naverMap
                    tag = item.code
                    this.setOnClickListener { overlay ->
                        onMarkerClick(overlay)
                        //naverMap.setCamera(LatLng(naverPoints[0].latitude,naverPoints[0].longitude),12.0)
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
    Text("${data.value.size}", fontSize = 50.sp)
}

fun NaverMap.initLocation() {
    addOnLocationChangeListener { location ->
        if (locationTrackingMode == LocationTrackingMode.Follow) {
            setCamera(LatLng(location.latitude, location.longitude),12.0)
        }
        locationTrackingMode = LocationTrackingMode.NoFollow
        removeOnLocationChangeListener { }
    }
}

private fun NaverMap.setCamera(camera:LatLng ,zoom:Double){
    moveCamera(
        CameraUpdate.scrollAndZoomTo(
            com.naver.maps.geometry.LatLng(
                camera.latitude,
                camera.longitude
            ),zoom
        ).animate(CameraAnimation.Easing)
    )
}

private fun Context.searchNaverMap(course: Course) {
    val url =
        "nmap://route/car?slat=${course.start.latitude}&slng=${course.start.longitude}&sname=start" +
                "&dlat=${course.goal.latitude}&dlng=${course.goal.longitude}&dname=end" +
                course.waypoints.run {
                    var str = ""
                    this.forEachIndexed { idx, latlng ->
                        str += "&v${idx + 1}lat=${latlng.latitude}&v${idx + 1}lng=${latlng.longitude}&v${idx + 1}name=v${idx + 1}"
                    }
                    str
                } +
                "&appname=com.dhkim139.wheretogo"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}

private fun Context.searchTMap(course: Course) {
    val api = TMapTapi(this)
    api.setSKTMapAuthentication(BuildConfig.TMAP_APP_KEY)

    api.setOnAuthenticationListener(object : OnAccountsUpdateListener,
        TMapTapi.OnAuthenticationListenerCallback {
        override fun onAccountsUpdated(p0: Array<out Account>?) {

        }

        override fun SKTMapApikeySucceed() {
            val routeMap = HashMap<String, String>()

            course.start.apply {
                routeMap["rStName"] = "출발지"
                routeMap["rStX"] = longitude.toString()
                routeMap["rStY"] = latitude.toString()
            }

            course.goal.apply {
                routeMap["rGoName"] = "목적지"
                routeMap["rGoX"] = longitude.toString()
                routeMap["rGoY"] = latitude.toString()
            }

            course.waypoints.forEachIndexed { idx, latlng ->
                routeMap["rV${idx + 1}Name"] = "경유지 ${idx + 1}"
                routeMap["rV${idx + 1}X"] = latlng.longitude.toString()
                routeMap["rV${idx + 1}Y"] = latlng.latitude.toString()
            }

            if (!api.isTmapApplicationInstalled) {
                api.tMapDownUrl?.let {
                    openPlayStore(it[0])
                    Log.d("tst", "${api.tMapDownUrl}")
                }
            } else {
                api.invokeRoute(routeMap)
            }
        }

        override fun SKTMapApikeyFailed(p0: String?) {
        }
    })


}

private fun Context.openPlayStore(url: String) {

    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
        setPackage("com.android.vending")
    }
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(webIntent)
    }
}

private fun Context.getMyLocationSource(): FusedLocationSource {
    return FusedLocationSource(this as ComponentActivity, 1000)
}