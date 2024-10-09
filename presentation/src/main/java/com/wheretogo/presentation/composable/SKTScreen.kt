package com.wheretogo.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.wheretogo.presentation.viewmodel.DriveViewModel
import com.naver.maps.geometry.LatLng
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapPolyLine
import com.skt.Tmap.TMapView
import com.wheretogo.presentation.c1
import com.wheretogo.presentation.model.toNaver
import com.wheretogo.presentation.BuildConfig

import kotlinx.coroutines.launch

@Composable
fun SKTScreen(displayMaxWidth: Dp, viewModel: DriveViewModel = hiltViewModel()) {
    var data by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    LaunchedEffect(Unit) {

    }
    Column(
        modifier = Modifier.width(displayMaxWidth),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TMapView2(data)
    }
}

@Composable
fun TMapView2(data:List<LatLng>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val mapView = remember {
        TMapView(context).apply {
            setSKTMapApiKey(BuildConfig.TMAP_APP_KEY)
            setLanguage(TMapView.LANGUAGE_KOREAN)
            zoomLevel = 13
            mapType = TMapView.MAPTYPE_STANDARD
            setLocationPoint(127.10069878544695,37.24049254419747)
            setCenterPoint(127.10069878544695,37.24049254419747)
        }
    }

    val lifecycleObserver = remember {
        LifecycleEventObserver { source, event ->

            coroutineScope.launch {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {}
                    Lifecycle.Event.ON_START -> {}
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> {}
                    Lifecycle.Event.ON_DESTROY -> {}
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
    update={
        it.apply {
            if(data.size>2) {
                setLocationPoint(data[0].longitude, data[0].latitude)
                setCenterPoint(data[0].longitude, data[0].latitude)
                val line = TMapPolyLine().apply {
                    data.map { TMapPoint(it.latitude, it.longitude) }.forEach {
                        addLinePoint(it)
                    }
                }
                addTMapPolyLine("line1", line)
            }
        }
    })
}
