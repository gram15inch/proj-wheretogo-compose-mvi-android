package com.dhkim139.wheretogo.ui.composable

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dhkim139.wheretogo.BuildConfig
import com.dhkim139.wheretogo.data.datasource.service.NaverMapApiService
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.PathOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun NaverScreen(displayMaxWidth: Dp) {
    Column(
        modifier = Modifier.width(displayMaxWidth), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NaverMapComposable()
    }

}

@Composable
fun NaverMapComposable() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val mapView = remember {
        MapView(context).apply {
            getMapAsync { naverMap ->
                val path = PathOverlay()
                path.coords = listOf(
                    LatLng(37.57152, 126.97714),
                    LatLng(37.56607, 126.98268),
                    LatLng(37.56445, 126.97707),
                    LatLng(37.55855, 126.97822)
                )
                path.map = naverMap
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

    AndroidView(modifier = Modifier.height(400.dp),factory = { mapView })
}
