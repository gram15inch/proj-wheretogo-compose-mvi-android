package com.dhkim139.wheretogo.ui.composable

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dhkim139.wheretogo.viewmodel.DriveViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.PathOverlay
import kotlinx.coroutines.launch

@Composable
fun NaverScreen(displayMaxWidth: Dp, viewModel: DriveViewModel = hiltViewModel()) {
    var data by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    LaunchedEffect(Unit) {
        data = viewModel.callApi()
    }
    Column(
        modifier = Modifier.width(displayMaxWidth), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NaverMapComposable(data)
        Text(
            text = data.size.toString(),
            modifier = Modifier.padding(start = 8.dp)
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
                    val cameraUpdate = CameraUpdate.scrollTo(data[data.size/2])
                    naverMap.moveCamera(cameraUpdate)
                    val path = PathOverlay()
                    path.coords = data
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

        AndroidView(modifier = Modifier.height(400.dp), factory = { mapView })
    }
}
