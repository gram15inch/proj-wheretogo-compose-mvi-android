package com.dhkim139.wheretogo.ui.composable

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dhkim139.wheretogo.BuildConfig
import com.dhkim139.wheretogo.data.datasource.dummy.c2
import com.dhkim139.wheretogo.data.model.map.Course
import com.dhkim139.wheretogo.domain.toNaver
import com.dhkim139.wheretogo.viewmodel.DriveViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.skt.Tmap.TMapTapi
import kotlinx.coroutines.launch


@Composable
fun NaverScreen(displayMaxWidth: Dp, viewModel: DriveViewModel = hiltViewModel()) {
    var data by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        data = viewModel.getMap(c2).points.toNaver()
    }
    Column(
        modifier = Modifier.width(displayMaxWidth), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NaverMapComposable(data)
        Text(
            text = data.size.toString(),
            modifier = Modifier
                .padding(start = 8.dp)
        )
        Text(
            modifier = Modifier.clickable {
                context.searchNaverMap(c2)
            },
            text = "네이버지도에서 찾기", fontSize = 20.sp
        )
        Text(
            modifier = Modifier.clickable {
                context.searchTMap(c2)
            },
            text = "티맵에서 찾기", fontSize = 20.sp
        )
    }

}

@Composable
fun NaverMapComposable(data: List<LatLng>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    if (data.size > 2) {
        val mapView = remember {
            MapView(context).apply {
                getMapAsync { naverMap ->
                    naverMap.cameraPosition = CameraPosition(
                        LatLng(37.5666102, 126.9783881),
                        12.0,
                    )
                }
            }
        }

        val lifecycleObserver = remember {
            LifecycleEventObserver { source, event ->

                coroutineScope.launch {
                    when (event) {
                        Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                        Lifecycle.Event.ON_START -> mapView.onStart()
                        Lifecycle.Event.ON_RESUME -> mapView.onResume()
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

        AndroidView(modifier = Modifier.height(400.dp), factory = { mapView },
            update = {
                it.getMapAsync { naverMap ->
                    val cameraUpdate = CameraUpdate.scrollTo(data[0])
                    naverMap.moveCamera(cameraUpdate)
                    val marker = Marker()
                    marker.position = data[0]
                    marker.map = naverMap
                    val path = PathOverlay()
                    path.coords = data
                    path.map = naverMap
                }
            })
    }
}

fun Context.searchNaverMap(course: Course) {
    val url = "nmap://route/car?slat=${course.start.latitude}&slng=${course.start.longitude}&sname=start" +
                "&dlat=${course.goal.latitude}&dlng=${course.goal.longitude}&dname=end" +
                course.waypoints.run {
                    var str=""
                    this.forEachIndexed {idx,latlng->
                        str += "&v${idx+1}lat=${latlng.latitude}&v${idx+1}lng=${latlng.longitude}&v${idx+1}name=v${idx+1}"
                    }
                    str
                } +
                "&appname=com.dhkim139.wheretogo"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}

fun Context.searchTMap(course: Course) {
    val api = TMapTapi(this)
    api.setSKTMapAuthentication(BuildConfig.TMAP_APP_KEY)
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
        routeMap["rV${idx + 1}X"] = latlng.latitude.toString()
        routeMap["rV${idx + 1}Y"] = latlng.longitude.toString()
    }

    if(!api.isTmapApplicationInstalled)
        api.tMapDownUrl?.let {
            openPlayStore(it[0])
        }
    else{
        api.invokeRoute(routeMap)
        api.invokeTmap()
    }
    Log.d("tst","${ api.isTmapApplicationInstalled}")
    Log.d("tst","${ api.tMapDownUrl}")

}

private fun Context.openPlayStore(url:String) {

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